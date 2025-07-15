package com.meetcha.auth.service;

import com.meetcha.auth.dto.TokenResponseDto;
import com.meetcha.auth.entity.RefreshTokenEntity;
import com.meetcha.auth.entity.UserEntity;
import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.auth.repository.RefreshTokenRepository;
import com.meetcha.auth.repository.UserRepository;
import com.nimbusds.oauth2.sdk.TokenResponse;
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
                .orElseThrow(() -> new RuntimeException("유효하지 않은 refresh Token입니다."));

        if (entity.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("만료된 refresh Token입니다.");
        }

        UserEntity user = userRepository.findById(entity.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String newAccessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getUserId(), user.getEmail());

        entity.update(newRefreshToken, LocalDateTime.now().plusDays(14));
        refreshTokenRepository.save(entity);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }
}
