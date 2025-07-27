package com.meetcha.user.dto;

import java.time.LocalDateTime;

// 기존의 일정 조회 시 사용
public record scheduleResponse(
        String title,
        LocalDateTime startAt,
        LocalDateTime endAt
) {}
