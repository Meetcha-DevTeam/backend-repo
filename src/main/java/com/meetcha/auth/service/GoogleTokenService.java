package com.meetcha.auth.service;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleTokenService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    //항상 유효한 Google access token을 반환
    //만료 60초 전이면 refresh_token으로 자동 재발급
    //e로 시작하는 JWT 토큰아니고 구글 api 호출할때 쓰는 토큰

    public String ensureValidAccessToken(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1) access token 존재 체크
        if (user.getGoogleToken() == null || user.getGoogleToken().isBlank()) {
            throw new CustomException(ErrorCode.MISSING_GOOGLE_ACCESS_TOKEN);
        }

        // 2) 아직 유효하면 그대로 사용
        if (user.getGoogleTokenExpiresAt() != null &&
                user.getGoogleTokenExpiresAt().isAfter(LocalDateTime.now().plusSeconds(60))) {
            return user.getGoogleToken();
        }

        // 3) refresh token 없으면 실패
        if (user.getGoogleRefreshToken() == null || user.getGoogleRefreshToken().isBlank()) {
            throw new CustomException(ErrorCode.MISSING_GOOGLE_REFRESH_TOKEN);
        }

        // 4) refresh 토큰으로 재발급
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", System.getenv("GOOGLE_CLIENT_ID"));
        form.add("client_secret", System.getenv("GOOGLE_CLIENT_SECRET"));
        form.add("refresh_token", user.getGoogleRefreshToken());
        form.add("grant_type", "refresh_token");

        @SuppressWarnings("unchecked")
        Map<String, Object> res = restTemplate.postForObject("https://oauth2.googleapis.com/token", form, Map.class);

        if (res == null || !res.containsKey("access_token")) {
            throw new CustomException(ErrorCode.GOOGLE_TOKEN_EXPIRED);
        }

        String newAccess = (String) res.get("access_token");
        long expiresIn = ((Number) res.getOrDefault("expires_in", 3600)).longValue();
        LocalDateTime newExpiry = LocalDateTime.now().plusSeconds(expiresIn);

        user.updateGoogleAccessToken(newAccess, newExpiry);
        userRepository.save(user);

        return newAccess;
    }

    /**
     * 최초 OAuth 교환 시 저장용 (코드 교환 후 access/refresh/expires_in을 넘겨 호출)
     */
    public void saveInitialGoogleTokens(UUID userId, String accessToken, String refreshToken, long expiresInSec) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateGoogleAllTokens(accessToken, refreshToken, LocalDateTime.now().plusSeconds(expiresInSec));
        userRepository.save(user);
    }
}
