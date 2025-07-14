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
        String Email = "kuit@naver.com";
        String Name = "kuit";
        String GoogleToken = "mock-google-token-123";
        String ProfileImage = "https://example.com/profile.png";
       Optional<UserEntity> optionalUser = userRepository.findByEmail(Email);

        UserEntity user = UserEntity.builder()
                .name(Name)
                .email(Email)
                .googleToken(GoogleToken)
                .createdAt(LocalDateTime.now())
                .profileImgSrc(ProfileImage)
                .build();

        userRepository.save(user);


        String accessToken = "mock-access-token-abc";
        String refreshToken = "mock-refresh-token-xyz";

        return new LoginResponseDto(accessToken, refreshToken);
    }
}
