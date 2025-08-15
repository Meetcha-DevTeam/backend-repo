package com.meetcha.joinmeeting.dto;

import java.time.LocalDateTime;
import java.util.List;

public record JoinMeetingRequest(
        String nickname,
        List<TimeSlot> selectedTimes
) {
    public record TimeSlot(
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {}
}
