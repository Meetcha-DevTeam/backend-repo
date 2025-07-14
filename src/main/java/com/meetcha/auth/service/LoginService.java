package com.meetcha.auth.service;

import com.meetcha.auth.config.GoogleOAuthProperties;
import com.meetcha.auth.dto.LoginRequestDto;
import com.meetcha.auth.dto.LoginResponseDto;
import com.meetcha.auth.entity.UserEntity;
import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.auth.repository.UserRepository;
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

    public LoginResponseDto googleLogin(LoginRequestDto request) {
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

        ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                "https://oauth2.googleapis.com/token",
                HttpMethod.POST,
                tokenRequest,
                Map.class
        );

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("구글 토큰 요청 실패");
        }

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(accessToken);

        HttpEntity<Void> userInfoRequest = new HttpEntity<>(userInfoHeaders);

        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                userInfoRequest,
                Map.class
        );

        if (!userInfoResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("구글 유저 정보 요청 실패");
        }

        Map<String, Object> userInfo = userInfoResponse.getBody();
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String picture = (String) userInfo.get("picture");

        Optional<UserEntity> optionalUser = userRepository.findByEmail(email);

        UserEntity user = optionalUser.orElseGet(() -> {
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

        return new LoginResponseDto(jwtAccessToken, jwtRefreshToken);
    }
}
