package com.meetcha.auth.controller;

import com.meetcha.auth.dto.*;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.auth.service.LoginService;
import com.meetcha.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class UserController {
    private final LoginService loginService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<TokenResponseDto>> googleLogin(@RequestBody LoginRequestDto request){
        TokenResponseDto response = loginService.googleLogin(request);
        return ResponseEntity
                .ok(ApiResponse.success(200, "구글 로그인에 성공했습니다.", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponseDto>> refresh(@RequestBody RefreshTokenRequestDto request) {
        TokenResponseDto tokenResponse = refreshTokenService.reissueAccessToken(request.getRefreshToken());
        return ResponseEntity
                .ok(ApiResponse.success(200, "accessToken 재발급에 성공했습니다.", tokenResponse));
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<TestLoginResponse>> testLogin(
            @RequestBody TestLoginRequest testLoginRequest) {
        TestLoginResponse response = loginService.testLogin(testLoginRequest);
        return ResponseEntity.ok(ApiResponse.success(200, "accessToken 발급했습니다.", response));
    }

}
