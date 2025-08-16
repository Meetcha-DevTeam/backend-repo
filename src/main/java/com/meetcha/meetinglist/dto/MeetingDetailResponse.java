package com.meetcha.meetinglist.dto;

import com.meetcha.meeting.domain.MeetingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MeetingDetailResponse {
    private UUID meetingId;
    private String title;
    private String description;
    private MeetingStatus meetingStatus;
    private LocalDateTime deadline;
    private Integer durationMinutes;
    private LocalDateTime confirmedTime;
    private String meetingCode;
    private List<ParticipantDto> participants;
}
