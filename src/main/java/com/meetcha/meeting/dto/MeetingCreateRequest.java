package com.meetcha.meeting.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MeetingCreateRequest (
    String title,
    String description,
    int durationMinutes,
    List<LocalDate> candidateDates,
    LocalDateTime deadline,
    UUID projectId) {}
