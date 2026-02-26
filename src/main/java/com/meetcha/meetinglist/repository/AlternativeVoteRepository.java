package com.meetcha.meetinglist.repository;

import com.meetcha.meetinglist.domain.AlternativeVoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlternativeVoteRepository extends JpaRepository<AlternativeVoteEntity, UUID> {

    // 특정 유저가 특정 대안 시간에 투표했는지 (기존 투표 조회)
    Optional<AlternativeVoteEntity> findByAlternativeTimeIdAndUserId(UUID alternativeTimeId, UUID userId);

    // 해당 대안 시간에 체크(true)된 인원 수
    int countByAlternativeTimeIdAndCheckedTrue(UUID alternativeTimeId);

    // 유저가 특정 대안 시간에 체크한 적 있는지
    boolean existsByAlternativeTimeIdAndUserIdAndCheckedTrue(UUID alternativeTimeId, UUID userId);

    // 유저가 특정 미팅에서 이미 어떤 시간에든 투표했는지 (추가 조건)
    boolean existsByUserIdAndCheckedTrue(UUID userId);

    boolean existsByAlternativeTime_MeetingIdAndUserIdAndCheckedTrue(UUID meetingId, UUID userId);

    void deleteByAlternativeTime_MeetingId(UUID meetingId);

    // 특정 미팅에서 가장 많이 선택된 대안시간 찾기
    @Query("""
    SELECT t.alternativeTimeId, COUNT(v)
    FROM AlternativeVoteEntity v
    JOIN v.alternativeTime t
    WHERE t.meetingId = :meetingId
      AND v.checked = true
    GROUP BY t.alternativeTimeId
    ORDER BY COUNT(v) DESC
""")
    List<Object[]> findTopVotedAlternativeTime(UUID meetingId);

    @Query("""
    SELECT COUNT(DISTINCT v.userId)
    FROM AlternativeVoteEntity v
    JOIN v.alternativeTime t
    WHERE t.meetingId = :meetingId
      AND v.checked = true
""")
    int countDistinctVoters(UUID meetingId);

}
