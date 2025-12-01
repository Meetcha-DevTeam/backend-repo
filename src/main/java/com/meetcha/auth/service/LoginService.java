package com.meetcha.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final GoogleOAuthProperties googleProps;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DEFAULT_PROFILE_IMG =
            "https://lh3.googleusercontent.com/a/ACg8ocLqlP1MkWIf-XdpRmZc6CILZLIomiOQ88KoZFHTilcFtGYgrA=s96-c"; // 임시로..

    public TokenResponseDto googleLogin(LoginRequestDto request) {

        String code = request.getCode();
        String redirectUrl = request.getRedirectUri();
        RestTemplate restTemplate = new RestTemplate();

        /* ========== 1) 구글 토큰 요청 ========== */
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleProps.getClientId());
        params.add("client_secret", googleProps.getClientSecret());
        params.add("redirect_uri", redirectUrl);
        params.add("grant_type", "authorization_code");

        ResponseEntity<Map> tokenResponse;
        try {
            tokenResponse = restTemplate.exchange(
                    "https://oauth2.googleapis.com/token",
                    HttpMethod.POST,
                    new HttpEntity<>(params, tokenHeaders),
                    Map.class
            );
        } catch (Exception e) {
            log.error("[OAuth] token exchange ERROR: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INVALID_GOOGLE_CODE);
        }

        if (tokenResponse.getBody() == null) {
            log.error("[OAuth] Token response body NULL");
            throw new CustomException(ErrorCode.GOOGLE_TOKEN_REQUEST_FAILED);
        }

        Map<String, Object> tokenBody = tokenResponse.getBody();
        String googleAccessToken = (String) tokenBody.get("access_token");
        String googleRefreshToken = (String) tokenBody.get("refresh_token");
        String idToken = (String) tokenBody.get("id_token");

        long expiresInSec = ((Number) tokenBody.getOrDefault("expires_in", 3600)).longValue();
        LocalDateTime accessTokenExpiry = LocalDateTime.now().plusSeconds(expiresInSec);


        /* ========== 2) userinfo 호출 ========== */
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(googleAccessToken);

        ResponseEntity<Map> userInfoResponse;
        try {
            userInfoResponse = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    new HttpEntity<>(userInfoHeaders),
                    Map.class
            );
        } catch (Exception e) {
            log.error("[OAuth] Google userinfo request ERROR: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.GOOGLE_USERINFO_REQUEST_FAILED);
        }

        if (userInfoResponse.getBody() == null) {
            log.error("[OAuth] userinfo body NULL");
            throw new CustomException(ErrorCode.GOOGLE_USERINFO_REQUEST_FAILED);
        }

        Map<String, Object> userInfo = userInfoResponse.getBody();
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");

        /* ========== 3) picture 결정 (userinfo → id_token → fallback) ========== */
        String picture = (String) userInfo.get("picture");
        log.info("[OAuth] userinfo.picture = {}", picture);

        if (picture == null || picture.isBlank()) {
            picture = extractPictureFromIdToken(idToken);
            log.info("[OAuth] id_token.picture = {}", picture);
        }

        if (picture == null || picture.isBlank()) {
            picture = DEFAULT_PROFILE_IMG;
            log.info("[OAuth] FALLBACK picture = {}", picture);
        }

        final String finalPicture = picture; // 🔥 람다에서 사용하려면 final 필요


        /* ========== 4) 유저 조회 or 생성 ========== */
        UserEntity user = userRepository.findByEmail(email).orElseGet(() -> {
            UserEntity newUser = UserEntity.builder()
                    .email(email)
                    .name(name)
                    .googleToken(googleAccessToken)
                    .googleRefreshToken(googleRefreshToken)
                    .googleTokenExpiresAt(accessTokenExpiry)
                    .profileImgSrc(finalPicture) // ✔ 람다 내부에서도 문제 없음
                    .createdAt(LocalDateTime.now())
                    .build();
            return userRepository.save(newUser);
        });

        /* ========== 로그인 때마다 필드 업데이트 ========== */
        user.setName(name);
        user.setProfileImgSrc(finalPicture);

        if (googleRefreshToken != null && !googleRefreshToken.isBlank()) {
            user.updateGoogleAllTokens(googleAccessToken, googleRefreshToken, accessTokenExpiry);
        } else {
            user.updateGoogleAccessToken(googleAccessToken, accessTokenExpiry);
        }

        userRepository.save(user);
        log.info("[OAuth] FINAL SAVED PICTURE IN DB = {}", user.getProfileImgSrc());


        /* ========== 5) JWT 발급 ========== */
        String jwtAccessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());
        String jwtRefreshToken = jwtProvider.createRefreshToken(user.getUserId(), user.getEmail());

        refreshTokenRepository.findByUserId(user.getUserId())
                .ifPresentOrElse(
                        existing -> {
                            existing.update(jwtRefreshToken, LocalDateTime.now().plusDays(14));
                            refreshTokenRepository.save(existing);
                        },
                        () -> refreshTokenRepository.save(
                                new RefreshTokenEntity(
                                        user.getUserId(),
                                        jwtRefreshToken,
                                        LocalDateTime.now().plusDays(14)
                                )
                        )
                );

        return new TokenResponseDto(jwtAccessToken, jwtRefreshToken);
    }

    /* ========== id_token에서 picture 추출 ========== */
    private String extractPictureFromIdToken(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

            Map<String, Object> map = objectMapper.readValue(payloadJson, Map.class);
            return (String) map.get("picture");
        } catch (Exception e) {
            log.warn("[OAuth] Failed to parse id_token picture: {}", e.getMessage());
            return null;
        }
    }

    /* ========== 테스트 로그인 ========== */
    public TestLoginResponse testLogin(TestLoginRequest req) {
        UserEntity user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String token = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());
        log.info("[OAuth] Saved profileImgSrc = {}", user.getProfileImgSrc());
        return new TestLoginResponse(token);
    }

    /* ========== 테스트용 더미 메서드(loadImageAsStream) ========== */
    // 테스트 충돌나는것때문에 임시로 추가 추후에 지울게요
    public InputStream loadImageAsStream(String url) throws IOException {
        return InputStream.nullInputStream();
    }
}
