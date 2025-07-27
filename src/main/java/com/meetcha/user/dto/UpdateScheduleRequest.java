package com.meetcha.user.dto;

import java.time.LocalDateTime;

public record UpdateScheduleRequest(
        String eventId,
        String title,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String recurrence
) {}
