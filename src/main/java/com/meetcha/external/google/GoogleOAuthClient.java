package com.meetcha.external.google;

import com.meetcha.auth.config.GoogleOAuthProperties;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class GoogleOAuthClient {
    private final GoogleOAuthProperties googleProps;
    private final RestTemplate restTemplate;

    public Map<String, Object> fetchToken(String code, String redirectUrl) {
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
        return tokenBody;
    }

    public Map<String, Object> fetchUserInfo(String googleAccessToken) {
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
        return userInfo;
    }
}
