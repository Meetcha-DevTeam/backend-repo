package com.meetcha.reflection.domain;

import com.meetcha.reflection.dto.GetReflectionResponse;
import com.meetcha.reflection.dto.GetWrittenReflectionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeetingReflectionRepository extends JpaRepository<MeetingReflectionEntity, UUID> {
    //사용자가 이 미팅에 대해 이미 회고를 작성했는지 확인
    Optional<MeetingReflectionEntity> findByMeeting_MeetingIdAndUser_UserId(UUID meetingId, UUID userId);

    //해당 미팅에 대한 회고가 존재하는지 확인
    boolean existsByMeeting_MeetingIdAndUser_UserId(UUID meetingId, UUID userId);

    //미팅 회고 목록 요약 조회
    //confirmedTime은 문자열 포맷으로 변환
    //회고 작성 시점 기준으로 정렬
    @Query("""
    SELECT new com.meetcha.reflection.dto.GetWrittenReflectionResponse(
         m.meetingId,
         p.projectId,
         COALESCE(a.customName, p.name),
         m.title,
         m.confirmedTime,
         r.completedWork,
         r.plannedWork
     )
    FROM MeetingReflectionEntity r
    JOIN r.meeting m
    LEFT JOIN m.project p
    LEFT JOIN UserProjectAliasEntity a
    ON a.project.projectId = p.projectId AND a.user.userId = r.user.userId
    WHERE r.user.userId = :userId
    ORDER BY r.createdAt DESC
    """)
    List<GetWrittenReflectionResponse> findWrittenReflectionByUserId(@Param("userId") UUID userId);

    //특정 회고 상세 조회
    @Query("""
    SELECT new com.meetcha.reflection.dto.GetReflectionResponse(
        m.meetingId,
        p.projectId,
        COALESCE(a.customName, p.name),
        m.title,
        m.description,
        m.confirmedTime,
        r.contribution,
        r.role,
        r.thought,
        r.completedWork,
        r.plannedWork
    )
    FROM MeetingReflectionEntity r
    JOIN r.meeting m
    LEFT JOIN m.project p
    LEFT JOIN UserProjectAliasEntity a
        ON a.project.projectId = p.projectId AND a.user.userId = r.user.userId
    WHERE m.meetingId = :meetingId AND r.user.userId = :userId
""")
    Optional<GetReflectionResponse> findReflectionDetailByMeetingIdAndUserId(
            @Param("meetingId") UUID meetingId,
            @Param("userId") UUID userId
    );


}
