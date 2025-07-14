package com.meetcha.auth.controller;

import com.meetcha.auth.dto.LoginApiResponse;
import com.meetcha.auth.dto.LoginRequestDto;
import com.meetcha.auth.dto.LoginResponseDto;
import com.meetcha.auth.service.LoginService;
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

    @PostMapping("/google")
    public ResponseEntity<LoginApiResponse<LoginResponseDto>> googleLogin(@RequestBody LoginRequestDto request){
        try{
            LoginResponseDto response = loginService.googleLogin(request);
            return ResponseEntity.ok(LoginApiResponse.success(200, "구글 로그인에 성공했습니다.", response));
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(401).body((LoginApiResponse.fail(401, "유효하지 않은 구글 인가 코드입니다.")));
        }
    }
}
