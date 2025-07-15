package com.meetcha.meeting.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MeetingInfoResponse(
        //미팅참여, 미팅정보확인 시점에 사용 됨
        UUID meetingId,
        String title,
        String description,
        LocalDateTime deadline,
        int durationMinutes
) {}
