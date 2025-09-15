package com.meetcha.auth.controller;

import com.meetcha.auth.dto.GoogleInitialTokenRequest;
import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.auth.service.GoogleTokenService;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.util.AuthHeaderUtils;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class GoogleAuthController {

    private final GoogleTokenService googleTokenService;
    private final JwtProvider jwtProvider;

    /**
     * 구글 OAuth 초기 토큰 저장 API
     * 호출 시점: 구글 로그인 + 동의 후, 프론트가 code로 토큰 교환을 끝낸 직후
     */
    @PostMapping("/{userId}/google/tokens")
    public ResponseEntity<ApiResponse<Void>> saveInitialGoogleTokens(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("userId") UUID pathUserId,
            @RequestBody @Valid GoogleInitialTokenRequest req
    ) {
        // 1) JWT에서 userId 추출
        String bearer = AuthHeaderUtils.extractBearerToken(authorizationHeader);
        UUID jwtUserId = jwtProvider.getUserId(bearer);

        // 2) Path의 userId와 일치하는지 검증
        if (!jwtUserId.equals(pathUserId)) {
            throw new CustomException(ErrorCode.FORBIDDEN); // 403
        }

        // 3) 최초 연동인데 refreshToken이 없으면 에러
        if (req.getRefreshToken() == null || req.getRefreshToken().isBlank()) {
            // 프론트 로그인 URL 점검 유도
            throw new CustomException(ErrorCode.MISSING_GOOGLE_REFRESH_TOKEN);
        }

        // 4) 저장
        googleTokenService.saveInitialGoogleTokens(
                pathUserId,
                req.getAccessToken(),
                req.getRefreshToken(),
                req.getExpiresInSec()
        );

        return ResponseEntity
                .ok(ApiResponse.success(200, "구글 OAuth 토큰이 성공적으로 저장되었습니다."));
    }
}
