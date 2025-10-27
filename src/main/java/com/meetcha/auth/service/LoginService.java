package com.meetcha.auth.service;

import com.meetcha.auth.config.GoogleOAuthProperties;
import com.meetcha.auth.dto.LoginRequestDto;
import com.meetcha.auth.dto.TestLoginRequest;
import com.meetcha.auth.dto.TestLoginResponse;
import com.meetcha.auth.dto.TokenResponseDto;
import com.meetcha.auth.domain.RefreshTokenEntity;
import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.auth.domain.RefreshTokenRepository;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final GoogleOAuthProperties googleProps;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenResponseDto googleLogin(LoginRequestDto request) {
        String code = request.getCode();
        String redirectUrl = request.getRedirectUri() + "/login-complete";
        RestTemplate restTemplate = new RestTemplate();


        // 구글 토큰 교환
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("code", code);
        tokenParams.add("client_id", googleProps.getClientId());
        tokenParams.add("client_secret", googleProps.getClientSecret());
        tokenParams.add("redirect_uri", redirectUrl);
        tokenParams.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> tokenRequest =
                new HttpEntity<>(tokenParams, tokenHeaders);

        ResponseEntity<Map> tokenResponse;
        try {
            tokenResponse = restTemplate.exchange(
                    "https://oauth2.googleapis.com/token",
                    HttpMethod.POST,
                    tokenRequest,
                    Map.class
            );
        } catch (Exception e) {
            log.error("[OAuth] token exchange ERROR: {}", e.toString(), e);
            throw new CustomException(ErrorCode.INVALID_GOOGLE_CODE);
        }

        if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
            log.error("[OAuth] token exchange non-2xx or empty body: status={}", tokenResponse.getStatusCodeValue());
            throw new CustomException(ErrorCode.GOOGLE_TOKEN_REQUEST_FAILED);
        }

        Map<String, Object> tokenBody = tokenResponse.getBody();
        String googleAccessToken = (String) tokenBody.get("access_token");
        String googleRefreshToken = (String) tokenBody.get("refresh_token"); // 최초 로그인시에만 내려올 수 있음

        // expires_in 값을 LocalDateTime으로 변환
        long expiresInSec = 3600L;
        Object expiresInObj = tokenBody.get("expires_in");
        if (expiresInObj instanceof Number n) {
            expiresInSec = n.longValue();
        } else if (expiresInObj != null) {
            try {
                expiresInSec = Long.parseLong(String.valueOf(expiresInObj));
            } catch (NumberFormatException ignored) {
            }
        }
        LocalDateTime accessTokenExpiry = LocalDateTime.now().plusSeconds(expiresInSec);

        // 구글 사용자 정보
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(googleAccessToken);
        HttpEntity<Void> userInfoRequest = new HttpEntity<>(userInfoHeaders);

        ResponseEntity<Map> userInfoResponse;
        try {
            userInfoResponse = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    userInfoRequest,
                    Map.class
            );
        } catch (Exception e) {
            throw new CustomException(ErrorCode.GOOGLE_USERINFO_REQUEST_FAILED);
        }

        if (!userInfoResponse.getStatusCode().is2xxSuccessful() || userInfoResponse.getBody() == null) {
            throw new CustomException(ErrorCode.GOOGLE_USERINFO_REQUEST_FAILED);
        }

        Map<String, Object> userInfo = userInfoResponse.getBody();
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String picture = (String) userInfo.get("picture");

        // 기존 유저 조회 or 생성
        UserEntity user = userRepository.findByEmail(email).orElseGet(() -> {
            UserEntity newUser = UserEntity.builder()
                    .email(email)
                    .name(name)
                    .googleToken(googleAccessToken)
                    .googleRefreshToken(googleRefreshToken)
                    .googleTokenExpiresAt(accessTokenExpiry)
                    .profileImgSrc(picture)
                    .createdAt(LocalDateTime.now())
                    .build();
            return userRepository.save(newUser);
        });

        // 항상 access_token 갱신, refresh_token은 새로 내려온 경우에만 교체
        if (googleRefreshToken != null && !googleRefreshToken.isBlank()) {
            user.updateGoogleAllTokens(googleAccessToken, googleRefreshToken, accessTokenExpiry);
        } else {
            user.updateGoogleAccessToken(googleAccessToken, accessTokenExpiry);
        }
        userRepository.save(user);

        String jwtAccessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());
        String jwtRefreshToken = jwtProvider.createRefreshToken(user.getUserId(), user.getEmail());

        // RefreshToken 저장(있으면 갱신, 없으면 생성)
        refreshTokenRepository.findByUserId(user.getUserId())
                .ifPresentOrElse(
                        existing -> {
                            existing.update(jwtRefreshToken, LocalDateTime.now().plusDays(14));
                            refreshTokenRepository.save(existing);
                        },
                        () -> refreshTokenRepository.save(
                                new RefreshTokenEntity(user.getUserId(), jwtRefreshToken, LocalDateTime.now().plusDays(14))
                        )
                );

        return new TokenResponseDto(jwtAccessToken, jwtRefreshToken);
    }


    public TestLoginResponse testLogin(TestLoginRequest testLoginRequest) {
        UserEntity user = userRepository.findByEmail(testLoginRequest.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String accessToken = jwtProvider.createAccessToken(user.getUserId(),
                user.getEmail());

        return new TestLoginResponse(accessToken);
    }
}