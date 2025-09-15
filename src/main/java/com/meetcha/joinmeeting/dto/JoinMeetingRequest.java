package com.meetcha.joinmeeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinMeetingRequest{
    private String nickname;
    private List<TimeSlot> selectedTimes;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSlot {
        private LocalDateTime startAt;
        private LocalDateTime endAt;
    }
}
