package com.meetcha.meeting.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MeetingCreateResponse (
    UUID meetingId,
    LocalDateTime createdAt) {}
