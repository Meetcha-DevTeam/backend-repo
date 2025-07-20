package com.meetcha.auth.service;

import com.meetcha.auth.config.GoogleOAuthProperties;
import com.meetcha.auth.dto.LoginRequestDto;
import com.meetcha.auth.dto.TokenResponseDto;
import com.meetcha.auth.entity.RefreshTokenEntity;
import com.meetcha.auth.entity.UserEntity;
import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.auth.repository.RefreshTokenRepository;
import com.meetcha.auth.repository.UserRepository;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.exception.InvalidGoogleCodeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final GoogleOAuthProperties googleProps;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenResponseDto googleLogin(LoginRequestDto request) {
        String code = request.getCode();
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("code", code);
        tokenParams.add("client_id", googleProps.getClientId());
        tokenParams.add("client_secret", googleProps.getClientSecret());
        tokenParams.add("redirect_uri", googleProps.getRedirectUri());
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
            throw new InvalidGoogleCodeException(ErrorCode.INVALID_GOOGLE_CODE);
        }

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            throw new InvalidGoogleCodeException(ErrorCode.GOOGLE_TOKEN_REQUEST_FAILED);
        }

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userInfoRequest = new HttpEntity<>(userInfoHeaders);

        ResponseEntity<Map> userInfoResponse;
        try {
            userInfoResponse = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    userInfoRequest,
                    Map.class
            );
        } catch (Exception e) { //예외발생
            throw new InvalidGoogleCodeException(ErrorCode.GOOGLE_USERINFO_REQUEST_FAILED);
        }

        //응답왔는데 200번대아님
        if (!userInfoResponse.getStatusCode().is2xxSuccessful()) {
            throw new InvalidGoogleCodeException(ErrorCode.GOOGLE_USERINFO_REQUEST_FAILED);
        }

        Map<String, Object> userInfo = userInfoResponse.getBody();
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String picture = (String) userInfo.get("picture");

        UserEntity user = userRepository.findByEmail(email).orElseGet(() -> {
            UserEntity newUser = UserEntity.builder()
                    .email(email)
                    .name(name)
                    .googleToken(accessToken)
                    .profileImgSrc(picture)
                    .createdAt(LocalDateTime.now())
                    .build();
            return userRepository.save(newUser);
        });

        String jwtAccessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());
        String jwtRefreshToken = jwtProvider.createRefreshToken(user.getUserId(), user.getEmail());

        RefreshTokenEntity tokenEntity = new RefreshTokenEntity(
                user.getUserId(),
                jwtRefreshToken,
                LocalDateTime.now().plusDays(14)
        );
        refreshTokenRepository.save(tokenEntity);

        return new TokenResponseDto(jwtAccessToken, jwtRefreshToken);
    }
}