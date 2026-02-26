package com.meetcha.auth.service;

import com.meetcha.auth.dto.TokenResponseDto;
import com.meetcha.auth.domain.RefreshTokenEntity;
import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.auth.domain.RefreshTokenRepository;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public TokenResponseDto reissueAccessToken(String refreshToken) {

        RefreshTokenEntity entity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> {
                    log.warn("[reissueAccessToken] Invalid refresh token used");
                    return new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
                });

        if (entity.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("[reissueAccessToken] Expired refresh token used. userId={}, expiry={}", entity.getUserId(), entity.getExpiryDate());
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        UserEntity user = userRepository.findById(entity.getUserId())
                .orElseThrow(() -> {
                    log.error("[reissueAccessToken] RefreshToken exists but User not found. userId={}", entity.getUserId());
                    return new CustomException(ErrorCode.USER_NOT_FOUND);
                });

        String newAccessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getUserId(), user.getEmail());

        entity.update(newRefreshToken, LocalDateTime.now().plusDays(14));
        refreshTokenRepository.save(entity);

        log.info("[reissueAccessToken] Reissued refresh token. userId = {}", entity.getUserId());
        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }
}
