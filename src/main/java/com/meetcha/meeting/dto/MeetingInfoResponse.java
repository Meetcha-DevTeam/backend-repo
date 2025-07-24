package com.meetcha.meeting.dto;

import com.meetcha.meeting.domain.MeetingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MeetingInfoResponse(
        //미팅참여, 미팅정보확인 시점에 사용 됨
        UUID meetingId,
        String title,
        String description,
        MeetingStatus meetingStatus,
        LocalDateTime deadline,
        Integer durationMinutes,
        LocalDateTime confirmedTime
) {}
