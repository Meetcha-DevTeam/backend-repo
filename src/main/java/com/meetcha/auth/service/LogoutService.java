package com.meetcha.auth.service;

import com.meetcha.auth.domain.RefreshTokenRepository;
import com.meetcha.auth.dto.LogoutResponseDto;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public LogoutResponseDto logout(UUID userId) {
        log.info("[Logout] Starting logout process for userId: {}", userId);

        // RefreshToken 삭제
        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        refreshToken -> {
                            refreshTokenRepository.delete(refreshToken);
                            log.info("[Logout] RefreshToken deleted for userId: {}", userId);
                        },
                        () -> {
                            log.warn("[Logout] No RefreshToken found for userId: {}", userId);
                            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
                        }
                );

        log.info("[Logout] Logout completed successfully for userId: {}", userId);
        return new LogoutResponseDto("로그아웃 성공");
    }
}
