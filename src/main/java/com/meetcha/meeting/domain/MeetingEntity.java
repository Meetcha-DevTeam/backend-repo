
package com.meetcha.meeting.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "meetings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MeetingEntity {

    @Id
    @GeneratedValue
    @Column(name = "meeting_id", nullable = false, updatable = false)
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

    @Column(name = "meeting_status", nullable = false)
    private String meetingStatus; // 시작전, 진행중, 완료

    @Column(name = "confirmed_time")
    private LocalDateTime confirmedTime;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "code", nullable = false, unique = true)
    private String code;
    // todo 미팅 생성 시 디폴트 값 설정

}
