package com.meetcha.meetinglist.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record FilteredMeetingResponse (
    UUID meetingId,
    String title,
    String description, //nullable
    UUID projectId, //nullable
    String projectName, //nullable
    LocalDateTime deadline,
    LocalDateTime confirmedTime,
    int durationMinutes,
    String meetingStatus
){}
