package com.meetcha.meetinglist.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alternative_times")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlternativeTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "alternative_times_id", nullable = false, updatable = false)
    private UUID alternativeTimeId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "duration_adjusted_minutes")
    private Integer durationAdjustedMinutes;

    @Column(name = "excluded_participants")
    private String excludedParticipants;

    @Column(name = "meeting_id", nullable = false)
    private UUID meetingId;

    public static AlternativeTimeEntity create(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer durationAdjustedMinutes,
            String excludedParticipants,
            UUID meetingId
    ) {
        if (startTime == null || endTime == null || meetingId == null) {
            throw new IllegalArgumentException("start/end/meetingId is required");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
        return AlternativeTimeEntity.builder()
                .startTime(startTime)
                .endTime(endTime)
                .durationAdjustedMinutes(durationAdjustedMinutes)
                .excludedParticipants(excludedParticipants)
                .meetingId(meetingId)
                .build();
    }
}
