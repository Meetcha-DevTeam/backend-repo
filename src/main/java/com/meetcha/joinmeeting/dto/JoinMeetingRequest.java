package com.meetcha.joinmeeting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    @NotEmpty(message = "선택 시간은 최소 하나 이상이어야 합니다.")
    @Valid
    private List<TimeSlot> selectedTimes;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSlot {
        @NotNull(message = "시작 시간은 필수입니다.")
        private LocalDateTime startAt;
        @NotNull(message = "종료 시간은 필수입니다.")
        private LocalDateTime endAt;
    }
}
