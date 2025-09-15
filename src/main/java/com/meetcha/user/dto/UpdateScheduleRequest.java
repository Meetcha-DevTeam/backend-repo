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
        String eventId;
        String title;
        LocalDateTime startAt;
        LocalDateTime endAt;
        String recurrence;
}
