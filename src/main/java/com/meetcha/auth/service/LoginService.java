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

    public LoginResponseDto googleLogin(LoginRequestDto request){
        String mockEmail = "kuit@gmail.com";
        String mockName = "kuit";
        String mockGoogleToken = "mock-google-token-123";
        String mockProfileImage = "https://example.com/profile.png";
        Optional<UserEntity> optionalUser = userRepository.findByEmail(mockEmail);

        UserEntity user = optionalUser.orElseGet(() -> {
            return userRepository.save(
                    UserEntity.builder()
                            .userId(UUID.randomUUID())
                            .email(mockEmail)
                            .name(mockName)
                            .googleToken(mockGoogleToken)
                            .profileImgSrc(mockProfileImage)
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        });

        String accessToekn = "mock-access-token-abc";
        String refreshToken = "mock-refresh-token-xyz";

        return new LoginResponseDto(accessToekn, refreshToken);
    }
}
