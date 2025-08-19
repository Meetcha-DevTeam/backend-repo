package com.meetcha.joinmeeting.domain;

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

    @Column(name = "meeting_id", nullable = false)
    private UUID meetingId;


    public static MeetingParticipant create(UUID userId, UUID meetingId, String nickname) {
        MeetingParticipant participant = new MeetingParticipant();
        participant.userId = userId;
        participant.meetingId = meetingId;
        participant.nickname = nickname;
        return participant;
    }
}
