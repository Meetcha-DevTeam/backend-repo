package com.meetcha.auth.controller;

import com.meetcha.global.dto.ApiResponse;
import com.meetcha.auth.dto.LoginRequestDto;
import com.meetcha.auth.dto.RefreshTokenRequestDto;
import com.meetcha.auth.dto.TokenResponseDto;
import com.meetcha.auth.service.LoginService;
import com.meetcha.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
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
    public TokenResponseDto googleLogin(@RequestBody LoginRequestDto request) {
        return loginService.googleLogin(request);
    }

    @PostMapping("/refresh")
    public TokenResponseDto refresh(@RequestBody RefreshTokenRequestDto request) {
        return refreshTokenService.reissueAccessToken(request.getRefreshToken());
    }
}