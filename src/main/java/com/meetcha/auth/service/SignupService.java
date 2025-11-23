package com.meetcha.auth.service;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class SignupService {
    private final UserRepository userRepository;

    public UserEntity signup(String email, String name, String googleAccessToken, String googleRefreshToken, LocalDateTime accessTokenExpiry,String finalS3ProfileUrl, LocalDateTime accessTokenExpiry1) {
        UserEntity newUser = UserEntity.builder()
                .email(email)
                .name(name)
                .googleToken(googleAccessToken)
                .googleRefreshToken(googleRefreshToken)
                .googleTokenExpiresAt(accessTokenExpiry)
                .profileImgUrl(finalS3ProfileUrl)
                .createdAt(LocalDateTime.now())
                .build();
        return userRepository.save(newUser);
    }
}
