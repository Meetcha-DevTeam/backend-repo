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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public TokenResponseDto reissueAccessToken(String refreshToken) {
        RefreshTokenEntity entity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (entity.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        UserEntity user = userRepository.findById(entity.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getUserId(), user.getEmail());

        entity.update(newRefreshToken, LocalDateTime.now().plusDays(14));
        refreshTokenRepository.save(entity);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }

    public void save(UserEntity user, String jwtRefreshToken) {
        refreshTokenRepository.findByUserId(user.getUserId()).ifPresentOrElse(existing -> {
            existing.update(jwtRefreshToken, LocalDateTime.now().plusDays(14));
            refreshTokenRepository.save(existing);
        }
        ,() -> {
            refreshTokenRepository.save(new RefreshTokenEntity(user.getUserId(), jwtRefreshToken, LocalDateTime.now().plusDays(14)));
        });
    }
}
