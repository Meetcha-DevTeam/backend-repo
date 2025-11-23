package com.meetcha.auth.service;

import com.meetcha.auth.dto.LoginRequestDto;
import com.meetcha.auth.dto.TestLoginRequest;
import com.meetcha.auth.dto.TestLoginResponse;
import com.meetcha.auth.dto.TokenResponseDto;
import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.external.google.GoogleOAuthClient;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.net.URL;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final AwsS3Service awsS3Service;
    private final GoogleOAuthClient googleOAuthClient;
    private final SignupService signupService;
    private final RefreshTokenService refreshTokenService;
    private final UpdateUserService updateUserService;

    public TokenResponseDto googleLogin(LoginRequestDto request) {
        String code = request.getCode();
        String redirectUrl = request.getRedirectUri() + "/login-complete";

        // 구글 토큰 교환
        Map<String, Object> tokenBody = googleOAuthClient.fetchToken(code, redirectUrl);
        String googleAccessToken = (String) tokenBody.get("access_token");
        String googleRefreshToken = (String) tokenBody.get("refresh_token"); // 최초 로그인시에만 내려올 수 있음

        // expires_in 값을 LocalDateTime으로 변환
        LocalDateTime accessTokenExpiry = getAccessTokenExpiry(tokenBody);

        // 구글 사용자 정보
        Map<String, Object> userInfo = googleOAuthClient.fetchUserInfo(googleAccessToken);
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String pictureUrl = (String) userInfo.get("picture");

        //s3에 프사 업로드
        String s3ProfileUrl = uploadProfileImage(pictureUrl);

        // 기존 유저 조회 or 생성
        UserEntity user = synchronizeUser(email, googleAccessToken, googleRefreshToken, accessTokenExpiry, s3ProfileUrl, name);

        String jwtAccessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());
        String jwtRefreshToken = jwtProvider.createRefreshToken(user.getUserId(), user.getEmail());

        // RefreshToken 저장 (있으면 갱신, 없으면 생성)
        refreshTokenService.save(user, jwtRefreshToken);

        return new TokenResponseDto(jwtAccessToken, jwtRefreshToken);
    }

    private static LocalDateTime getAccessTokenExpiry(Map<String, Object> tokenBody) {
        long expiresInSec = 3600L;
        Object expiresInObj = tokenBody.get("expires_in");
        if (expiresInObj instanceof Number n) {
            expiresInSec = n.longValue();
        }
        else if (expiresInObj != null) {
            try {
                expiresInSec = Long.parseLong(String.valueOf(expiresInObj));
            } catch (NumberFormatException ignored) {
            }
        }
        LocalDateTime accessTokenExpiry = LocalDateTime.now().plusSeconds(expiresInSec);
        return accessTokenExpiry;
    }

    private String uploadProfileImage(String pictureUrl) {
        String s3ProfileUrl = null;
        try (InputStream in = loadImageAsStream(pictureUrl)) {
            String fileName = awsS3Service.createUniqueFileName("google_profile.jpg");
            s3ProfileUrl = awsS3Service.uploadFromStream(in, fileName, "image/jpeg");
        } catch (Exception e) {
            log.warn("프로필 이미지 업로드 실패 (기본 이미지 사용): {}", e.getMessage());
        }
        return s3ProfileUrl;
    }

    public InputStream loadImageAsStream(String pictureUrl) throws IOException {
        return new URL(pictureUrl).openStream();
    }

    private UserEntity synchronizeUser(String email, String googleAccessToken, String googleRefreshToken, LocalDateTime accessTokenExpiry, String s3ProfileUrl, String name) {
        Optional<UserEntity> byEmail = userRepository.findByEmail(email);
        if(byEmail.isPresent()){
            return updateUserService.syncWithGoogleUserInfo(email, googleAccessToken, googleRefreshToken, accessTokenExpiry, s3ProfileUrl);
        }
        return signupService.signup(email, name, googleAccessToken, googleRefreshToken, accessTokenExpiry, s3ProfileUrl, accessTokenExpiry);
    }

    public TestLoginResponse testLogin(TestLoginRequest testLoginRequest) {
        UserEntity user = userRepository.findByEmail(testLoginRequest.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String accessToken = jwtProvider.createAccessToken(user.getUserId(),
                user.getEmail());

        return new TestLoginResponse(accessToken);
    }
}