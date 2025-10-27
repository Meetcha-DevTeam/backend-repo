package com.meetcha.meeting.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingCreateRequest {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    private String description;

    @Min(value = 1, message = "미팅 시간은 1분 이상이어야 합니다.")
    @Max(value = 719, message = "미팅 시간은 719분 이하여야 합니다.")
    private int durationMinutes;

    @NotNull(message = "후보 날짜는 필수입니다.")
    @Size(min = 1, max = 10, message = "후보 날짜는 1개 이상 10개 이하여야 합니다.")
    private List<@Future(message = "후보 날짜는 내일 이후여야 합니다.") LocalDate> candidateDates;

    @NotNull(message = "마감 시간은 필수입니다.")
    @Future(message = "마감 시간은 현재 이후여야 합니다.")
    private LocalDateTime deadline;
    private UUID projectId;
    public Optional<UUID> getProjectId() {
        return Optional.ofNullable(projectId);
    }
}