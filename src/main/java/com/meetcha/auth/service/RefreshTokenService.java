package com.meetcha.auth.service;

import com.meetcha.auth.dto.TokenResponseDto;
import com.meetcha.auth.domain.RefreshTokenEntity;
import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.auth.domain.RefreshTokenRepository;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.exception.RefreshTokenInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    public TokenResponseDto reissueAccessToken(String refreshToken) {
        RefreshTokenEntity entity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RefreshTokenInvalidException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (entity.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RefreshTokenInvalidException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        UserEntity user = userRepository.findById(entity.getUserId())
                .orElseThrow(() -> new RefreshTokenInvalidException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getUserId(), user.getEmail());

        entity.update(newRefreshToken, LocalDateTime.now().plusDays(14));
        refreshTokenRepository.save(entity);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }

}
