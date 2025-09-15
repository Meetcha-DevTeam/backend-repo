package com.meetcha.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingInfoResponse {
    //미팅참여, 미팅정보확인 시점에 사용 됨
    UUID meetingId;
    String title;
    String description;
    Integer durationMinutes;
    List<LocalDate> candidateDates;
    LocalDateTime deadline;
    LocalDateTime createdAt;
}
