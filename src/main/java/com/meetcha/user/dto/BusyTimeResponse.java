package com.meetcha.user.dto;

import java.time.LocalDateTime;

// 기존의 일정 조회 시 사용
public record BusyTimeResponse(
        LocalDateTime startAt,
        LocalDateTime endAt
) {}
