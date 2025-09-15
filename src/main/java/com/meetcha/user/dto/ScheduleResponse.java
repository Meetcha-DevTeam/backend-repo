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
// 기존의 일정 조회 시 사용
public class ScheduleResponse{
        String eventId;
        String title;
        LocalDateTime startAt;
        LocalDateTime endAt;
        String recurrence;
}