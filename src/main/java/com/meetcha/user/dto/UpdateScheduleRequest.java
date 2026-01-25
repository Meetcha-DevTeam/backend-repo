package com.meetcha.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateScheduleRequest{
        private String eventId;
        private String title;
        private LocalDateTime startAt;
        private LocalDateTime endAt;
        private String recurrence;
}
