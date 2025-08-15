package com.meetcha.meeting.dto;

import com.meetcha.meeting.domain.MeetingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MeetingInfoResponse(
        //미팅참여, 미팅정보확인 시점에 사용 됨
        UUID meetingId,
        String title,
        String description,
        Integer durationMinutes,
        List<LocalDate> candidateDates,
        String deadline,
        String createdAt
) {}
