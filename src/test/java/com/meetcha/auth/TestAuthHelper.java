package com.meetcha.auth;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.auth.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class TestAuthHelper {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    public String createTestUserAndGetToken() {
        UserEntity testUser = UserEntity.builder()
                .email("testuser@meetcha.com")
                .name("테스트유저")
                .googleToken("test_google_token")
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(testUser);

        String accessToken = jwtProvider.createAccessToken(
                testUser.getUserId(),
                testUser.getEmail()
        );

        return accessToken;
    }
}
