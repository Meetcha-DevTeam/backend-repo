package com.meetcha.meeting.dto;

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
    String title;
    String description;
    int durationMinutes;
    List<LocalDate> candidateDates;
    LocalDateTime deadline;
    private UUID projectId;
    public Optional<UUID> getProjectId() {
        return Optional.ofNullable(projectId);
    }
}