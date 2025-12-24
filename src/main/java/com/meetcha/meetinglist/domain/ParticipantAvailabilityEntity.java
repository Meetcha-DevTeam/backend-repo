package com.meetcha.meetinglist.domain;

import com.meetcha.meeting.domain.MeetingEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.BinaryJdbcType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "participant_availabilities",
        indexes = {
                @Index(name = "idx_pa_meeting_id", columnList = "meeting_id"),
                @Index(name = "idx_pa_participant_id", columnList = "participant_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ParticipantAvailabilityEntity {

    @Id
    @GeneratedValue
    @JdbcType(BinaryJdbcType.class)
    @Column(name = "availability_id", nullable = false)
    private UUID availabilityId;

    /**
     * 어떤 미팅에 대한 가능 시간인지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private MeetingEntity meeting;

    /**
     * 어떤 참여자의 가능 시간인지
     */
    @JdbcType(BinaryJdbcType.class)
    @Column(name = "participant_id", nullable = false)
    private UUID participantId;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;
}
