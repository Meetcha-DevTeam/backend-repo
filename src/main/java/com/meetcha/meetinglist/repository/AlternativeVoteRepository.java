/*package com.meetcha.meetinglist.repository;

import com.meetcha.meetinglist.domain.AlternativeVoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

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

    boolean existsByAlternativeTimeIdAndUserIdNotNull(UUID meetingId, UUID userId);
}
*/