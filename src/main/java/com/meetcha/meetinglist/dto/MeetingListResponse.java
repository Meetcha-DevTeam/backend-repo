package com.meetcha.meetinglist.dto;

import com.meetcha.meeting.domain.MeetingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MeetingListResponse {
    private UUID meetingId;
    private String title;
    private LocalDateTime deadline;
    private LocalDateTime confirmedTime;
    private int durationMinutes;
    private MeetingStatus meetingStatus;
}
