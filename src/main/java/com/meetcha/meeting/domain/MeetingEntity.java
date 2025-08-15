
package com.meetcha.meeting.domain;

import com.meetcha.project.domain.ProjectEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "meetings")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MeetingEntity {

    @Id
    @GeneratedValue
    @Column(name = "meeting_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID meetingId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_minutes")
    private Integer durationMinutes; // 30분 단위 저장

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_status", nullable = false)
    private MeetingStatus meetingStatus; // 시작전, 진행중, 완료

    @Column(name = "confirmed_time")
    private LocalDateTime confirmedTime;

    @Column(name = "meeting_code", nullable = false, unique = true)
    private String meetingCode;

/*    @Column(name = "code", nullable = false, unique = true)
    private String code;*/

    @Column(name = "alternative_deadline")
    private LocalDateTime alternativeDeadline;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "project_id", nullable = true, insertable = false, updatable = false)
    private UUID projectId;

    // todo 미팅 생성 시 디폴트 값 설정 -> Service에서 직접 UUID 생성 후 채워넣음

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    public boolean isDeadlinePassed() {
        return deadline != null && deadline.isBefore(LocalDateTime.now());
    }

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingCandidateDateEntity> candidateDates = new ArrayList<>();

}
