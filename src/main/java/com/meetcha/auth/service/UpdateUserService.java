package com.meetcha.auth.service;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.meetcha.global.exception.ErrorCode.USER_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class UpdateUserService {
    private final UserRepository userRepository;

    public UserEntity syncWithGoogleUserInfo(String email, String googleAccessToken, String googleRefreshToken, LocalDateTime accessTokenExpiry, String s3ProfileUrl) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 항상 access_token 갱신, refresh_token은 새로 내려온 경우에만 교체
        if (hasRefreshToken(googleRefreshToken)) {
            user.updateGoogleAllTokens(googleAccessToken, googleRefreshToken, accessTokenExpiry);
        } else {
            user.updateGoogleAccessToken(googleAccessToken, accessTokenExpiry);
        }

        //프로필 이미지 반영
        if (hasProfile(s3ProfileUrl)) {
            user.setProfileImgUrl(s3ProfileUrl);
        }

        return user;
    }

    private static boolean hasProfile(String s3ProfileUrl) {
        return s3ProfileUrl != null;
    }

    private static boolean hasRefreshToken(String googleRefreshToken) {
        return googleRefreshToken != null && !googleRefreshToken.isBlank();
    }
}
