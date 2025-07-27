package com.meetcha.user.dto;

import java.time.LocalDateTime;

public record CreateScheduleRequest(
        String title,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String recurrence
) {}
