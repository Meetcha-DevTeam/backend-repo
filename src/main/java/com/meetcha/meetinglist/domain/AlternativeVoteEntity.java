package com.meetcha.meetinglist.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "alternative_votes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlternativeVoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "alternative_votes_id", nullable = false, updatable = false)
    private UUID voteId;

    @Column(name = "checked", nullable = false)
    private boolean checked;

    @Column(name = "alternative_times_id", nullable = false)
    private UUID alternativeTimeId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alternative_times_id", insertable = false, updatable = false)
    private AlternativeTimeEntity alternativeTime;

    public static AlternativeVoteEntity create(UUID alternativeTimeId, UUID userId) {
        return AlternativeVoteEntity.builder()
                .alternativeTimeId(alternativeTimeId)
                .userId(userId)
                .checked(true)
                .build();
    }
}