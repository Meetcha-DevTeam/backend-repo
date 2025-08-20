package com.meetcha.meeting.domain;

import com.meetcha.project.domain.ProjectEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.BinaryJdbcType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "meetings",
        uniqueConstraints = @UniqueConstraint(name = "uk_meetings_meeting_code", columnNames = "meeting_code"),
        indexes = {
                @Index(name = "idx_meetings_created_by", columnList = "created_by"),
                @Index(name = "idx_meetings_project_id", columnList = "project_id")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MeetingEntity {

    @Id
    @GeneratedValue
    @JdbcType(BinaryJdbcType.class)
    @Column(name = "meeting_id", nullable = false)
    private UUID meetingId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes; // NOT NULL

    @Column(name = "deadline", nullable = false)
    private LocalDateTime deadline; // NOT NULL

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // NOT NULL

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_status", nullable = false)
    private MeetingStatus meetingStatus; // NOT NULL

    @Column(name = "confirmed_time")
    private LocalDateTime confirmedTime; // NULL

    @Column(name = "meeting_code", nullable = false, length = 64)
    private String meetingCode; // NOT NULL (Unique 제약은 @Table에서)

    @Column(name = "alternative_deadline")
    private LocalDateTime alternativeDeadline; // NULL

    @JdbcType(BinaryJdbcType.class)
    @Column(name = "created_by", nullable = false)
    private UUID createdBy; // NOT NULL

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    /** 읽기용 편의 getter */
    @Transient
    public UUID getProjectId() {
        return project != null ? project.getProjectId() : null;
    }

    public boolean isDeadlinePassed() {
        return deadline != null && deadline.isBefore(LocalDateTime.now());
    }

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MeetingCandidateDateEntity> candidateDates = new ArrayList<>();
}
