package com.meetcha.joinmeeting.dto;

import java.time.LocalDateTime;
import java.util.List;

public record JoinMeetingRequest(
        String nickname,
        List<TimeSlot> selectedTimes
) {
    public record TimeSlot(
            //todo time 관리 방식 의논 요
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {}
}
