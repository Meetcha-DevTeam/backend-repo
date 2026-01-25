package com.meetcha.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateScheduleRequest{
        @NotBlank(message = "일정 제목은 필수입니다.")
        private String title;

        @NotNull(message = "시작 시간은 필수입니다.")
        private LocalDateTime startAt;

        @NotNull(message = "종료 시간은 필수입니다.")
        private LocalDateTime endAt;

        private String recurrence;
}
