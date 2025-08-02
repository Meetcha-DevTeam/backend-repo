package com.meetcha.meeting.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeetingRepository extends JpaRepository<MeetingEntity, UUID> {
    Optional<MeetingEntity> findByCode(String code);

    //선택적 미팅 조회 api에서 사용하는 쿼리
    @Query("""
        SELECT m FROM MeetingEntity m
        JOIN ParticipantEntity p ON p.meeting = m
        WHERE p.userId = :userId
        AND (:status IS NULL OR m.meetingStatus = :status)
    """)
    List<MeetingEntity> findAllByParticipant(
            @Param("userId") UUID userId,
            @Param("status") MeetingStatus status
    );

    List<MeetingEntity> findByMeetingStatusAndConfirmedTimeBefore(MeetingStatus meetingStatus, LocalDateTime now);

    List<MeetingEntity> findByMeetingStatusAndDeadlineBefore(MeetingStatus meetingStatus, LocalDateTime now);

}
