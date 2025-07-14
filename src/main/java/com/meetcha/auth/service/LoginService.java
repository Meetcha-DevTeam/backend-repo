package com.meetcha.auth.service;

import com.meetcha.auth.dto.LoginRequestDto;
import com.meetcha.auth.dto.LoginResponseDto;
import com.meetcha.auth.entity.UserEntity;
import com.meetcha.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserRepository userRepository;

    public LoginResponseDto googleLogin(LoginRequestDto request) {
        String email = "kuit@google.com";
        String name = "kuit";
        String googleToken = "mock-google-token-12345";
        String profileImage = "https://example.com/profile.png";

        Optional<UserEntity> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            UserEntity newUser = UserEntity.builder()
                    .userId(UUID.randomUUID()) // UUID 명시!
                    .name(name)
                    .email(email)
                    .googleToken(googleToken)
                    .createdAt(LocalDateTime.now())
                    .profileImgSrc(profileImage)
                    .build();

            userRepository.save(newUser);
        }

        String accessToken = "mock-access-token-abc1";
        String refreshToken = "mock-refresh-token-xyz1";

        return new LoginResponseDto(accessToken, refreshToken);
    }
}