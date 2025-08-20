package com.meetcha.reflection.domain;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.meeting.domain.MeetingEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "meeting_reflections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"meeting_id", "user_id"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingReflectionEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "reflection_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID reflectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false, columnDefinition = "BINARY(16)")
    private MeetingEntity meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UserEntity user;

    @Column(name = "contribution", nullable = false)
    private int contribution;

    @Column(name = "thought", nullable = false, columnDefinition = "TEXT")
    private String thought;

    @Column(name = "role", nullable = false, columnDefinition = "TEXT")
    private String role;

    @Column(name = "completed_work", columnDefinition = "TEXT")
    private String completedWork;

    @Column(name = "planned_work", columnDefinition = "TEXT")
    private String plannedWork;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
