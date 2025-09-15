package com.meetcha.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleInitialTokenRequest {

    @NotBlank
    private String accessToken;

    // 최초 연동 땐 필수. (없으면 400 처리)
    private String refreshToken;

    @NotNull
    private Long expiresInSec;
}