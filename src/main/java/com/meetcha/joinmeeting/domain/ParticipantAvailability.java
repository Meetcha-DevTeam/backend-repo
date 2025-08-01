package com.meetcha.joinmeeting.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "participant_availabilities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParticipantAvailability {

    @Id
    @GeneratedValue
    @Column(name = "availability_id", nullable = false, updatable = false)
    private UUID availabilityId;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "meeting_id", nullable = false)
    private UUID meetingId;

    @Column(name = "participant_id", nullable = false)
    private UUID participantId;

    // 정적 생성 메서드
    public static ParticipantAvailability create(UUID participantId, UUID meetingId,
                                                 LocalDateTime startAt, LocalDateTime endAt) {
        ParticipantAvailability availability = new ParticipantAvailability();
        availability.participantId = participantId;
        availability.meetingId = meetingId;
        availability.startAt = startAt;
        availability.endAt = endAt;
        return availability;
    }

}
