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
// 단일 상세 일정 조회 응답
public class ScheduleDetailResponse{
        String eventId;
        String title;
        LocalDateTime startAt;
        LocalDateTime endAt;
        String recurrence;
}
