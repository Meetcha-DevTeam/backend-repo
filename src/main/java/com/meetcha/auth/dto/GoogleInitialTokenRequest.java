package com.meetcha.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GoogleInitialTokenRequest(
        @NotBlank String accessToken,
        String refreshToken,      // 최초 연동 땐 필수. (없으면 400 처리)
        @NotNull Long expiresInSec
) {}
