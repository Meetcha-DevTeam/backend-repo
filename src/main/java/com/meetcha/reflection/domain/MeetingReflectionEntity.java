package com.meetcha.reflection.domain;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.meeting.domain.MeetingEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "meeting_reflections", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"meeting_id", "user_id"})
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingReflectionEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "reflection_id", nullable = false)
    private UUID reflectionId;

    @ManyToOne
    @JoinColumn(name = "meeting_id", nullable = false)
    private MeetingEntity meeting;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "contribution")
    private String contribution;

    @Column(name = "role")
    private String role;

    @Column(name = "feedback")
    private String feedback;

    @Column(name = "tasks_done")
    private String tasksDone;

    @Column(name = "tasks_todo")
    private String tasksTodo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
