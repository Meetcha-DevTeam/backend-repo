package com.meetcha.meetinglist.repository;

import com.meetcha.meetinglist.domain.AlternativeTimeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlternativeTimeRepository extends JpaRepository<AlternativeTimeEntity, UUID> {
    Optional<AlternativeTimeEntity> findByMeetingIdAndStartTime(UUID meetingId, LocalDateTime localDateTime);

    List<AlternativeTimeEntity> findByMeetingId(UUID meetingId);

    //투표 수가 가장 많은 대안 시간 중 가장 빠른 시간을 반환
    @Query("""
                SELECT a FROM AlternativeTimeEntity a
                WHERE a.meetingId = :meetingId
                ORDER BY 
                    (SELECT COUNT(v) FROM AlternativeVoteEntity v WHERE v.alternativeTimeId = a.alternativeTimeId AND v.checked = true) DESC,
                    a.startTime ASC
            """)
    Optional<AlternativeTimeEntity> findTopByMeetingIdOrderByVoteCountDescStartTimeAsc(UUID meetingId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM AlternativeTimeEntity a WHERE a.meetingId = :meetingId")
    void deleteByMeetingId(@Param("meetingId") UUID meetingId);



}
