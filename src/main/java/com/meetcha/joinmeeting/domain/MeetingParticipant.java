package com.meetcha.joinmeeting.domain;

import com.meetcha.meeting.domain.MeetingEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "meeting_participants")
@Getter
@NoArgsConstructor
public class MeetingParticipant {

    @Id
    @GeneratedValue
    @Column(name = "participant_id", nullable = false, updatable = false)
    private UUID participantId;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private MeetingEntity meeting;

    public static MeetingParticipant create(UUID userId, MeetingEntity meeting, String nickname) {
        MeetingParticipant participant = new MeetingParticipant();
        participant.userId = userId;
        participant.meeting = meeting;
        participant.nickname = nickname;
        return participant;
    }
}
