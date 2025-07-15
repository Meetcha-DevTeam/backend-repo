package com.meetcha.meeting.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MeetingRepository extends JpaRepository<MeetingEntity, UUID> {

//    Optional<MeetingEntity> findByMeetingId(UUID meetingId);

    Optional<MeetingEntity> findByCode(String code);
}
