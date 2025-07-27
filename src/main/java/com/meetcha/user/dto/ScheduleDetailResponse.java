package com.meetcha.user.dto;

import java.time.LocalDateTime;

// 단일 상세 일정 조회 응답
public record ScheduleDetailResponse(
        String eventId,
        String title,
        LocalDateTime startAt,
        LocalDateTime endAt
) {}
