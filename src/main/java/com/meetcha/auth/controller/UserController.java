package com.meetcha.auth.controller;

import com.meetcha.global.dto.ApiResponse;
import com.meetcha.auth.dto.LoginRequestDto;
import com.meetcha.auth.dto.RefreshTokenRequestDto;
import com.meetcha.auth.dto.TokenResponseDto;
import com.meetcha.auth.service.LoginService;
import com.meetcha.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class UserController {
    private final LoginService loginService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<TokenResponseDto>> googleLogin(@RequestBody LoginRequestDto request){
        try{
            TokenResponseDto response = loginService.googleLogin(request);
            return ResponseEntity.ok(ApiResponse.success(200, "구글 로그인에 성공했습니다.", response));
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(401).body(ApiResponse.fail(401, "유효하지 않은 구글 인가 코드입니다.", null));
        }
    }
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponseDto>> refresh(@RequestBody RefreshTokenRequestDto request) {
        try {
            TokenResponseDto tokenResponse = refreshTokenService.reissueAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success(200, "accessToken 재발급에 성공했습니다.", tokenResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.fail(401, e.getMessage(), null));
        }
    }

}
