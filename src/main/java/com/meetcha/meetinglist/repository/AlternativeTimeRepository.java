package com.meetcha.meetinglist.repository;

import com.meetcha.meetinglist.domain.AlternativeTimeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlternativeTimeRepository extends JpaRepository<AlternativeTimeEntity, UUID> {
    Optional<AlternativeTimeEntity> findByMeetingIdAndStartTime(UUID meetingId, LocalDateTime localDateTime);

    List<AlternativeTimeEntity> findByMeetingId(UUID meetingId);
}
