package com.meetcha.meeting.domain;

import com.meetcha.reflection.domain.MeetingReflectionEntity;
import com.meetcha.joinmeeting.domain.MeetingParticipant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeetingRepository extends JpaRepository<MeetingEntity, UUID> {
    Optional<MeetingEntity> findByMeetingCode(String code);

    //작성이 필요한 미팅조회 api에서 사용하는 쿼리
    @Query("""
    SELECT DISTINCT m FROM MeetingEntity m
    WHERE m.meetingStatus = :status
      AND (
          m.createdBy = :userId
          OR EXISTS (
              SELECT 1 FROM MeetingParticipant p
              WHERE p.meeting = m AND p.userId = :userId
          )
      )
      AND NOT EXISTS (
          SELECT 1 FROM MeetingReflectionEntity r
          WHERE r.meeting = m AND r.user.userId = :userId
      )
""")
    List<MeetingEntity> getMeetingsNeedReflection(
            @Param("userId") UUID userId,
            @Param("status") MeetingStatus status
    );

    //기여도할일조회api에서 사용하는 쿼리
    @Query("""
    SELECT COUNT(m) FROM MeetingEntity m
    WHERE m.meetingStatus = :status
      AND (
          m.createdBy = :userId
          OR EXISTS (
              SELECT 1 FROM MeetingParticipant p
              WHERE p.meeting = m AND p.userId = :userId
          )
      )
      AND NOT EXISTS (
          SELECT 1 FROM MeetingReflectionEntity r
          WHERE r.meeting = m AND r.user.userId = :userId
      )
""")
    long countMeetingsNeedReflection(
            @Param("userId") UUID userId,
            @Param("status") MeetingStatus status
    );


    // 투표 마감 후 가장 많이 투표된 대안 시간을 확정 시 사용
    @Query("""
    SELECT m FROM MeetingEntity m
    WHERE m.alternativeDeadline IS NOT NULL
      AND m.confirmedTime IS NULL
      AND m.alternativeDeadline < CURRENT_TIMESTAMP
      AND m.meetingStatus = 'MATCHING'
""")
    List<MeetingEntity> findMeetingsToConfirmFromAlternative();

    List<MeetingEntity> findByMeetingStatusAndConfirmedTimeBefore(MeetingStatus meetingStatus, LocalDateTime now);

    List<MeetingEntity> findByMeetingStatusAndDeadlineBefore(MeetingStatus meetingStatus, LocalDateTime now);

    //미팅목록조회쿼리
    @Query("""
    SELECT DISTINCT m
    FROM MeetingEntity m
    WHERE m.createdBy = :userId
       OR EXISTS (
           SELECT 1 FROM MeetingParticipant p
           WHERE p.meeting = m AND p.userId = :userId
       )
    ORDER BY m.createdAt DESC
""")
    List<MeetingEntity> findMyMeetings(@Param("userId") UUID userId);



    // (1) 단건 조회 + PESSIMISTIC_WRITE 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MeetingEntity m WHERE m.meetingId = :id")
    Optional<MeetingEntity> findByIdForUpdate(@Param("id") UUID id);

    // (2) 대안 시간 확정 대상들 조회 + PESSIMISTIC_WRITE 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT m FROM MeetingEntity m
        WHERE m.alternativeDeadline IS NOT NULL
          AND m.confirmedTime IS NULL
          AND m.alternativeDeadline < CURRENT_TIMESTAMP
          AND m.meetingStatus = 'MATCHING'
    """)
    List<MeetingEntity> findMeetingsToConfirmFromAlternativeForUpdate();

}