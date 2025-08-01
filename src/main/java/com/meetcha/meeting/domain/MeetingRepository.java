package com.meetcha.meeting.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeetingRepository extends JpaRepository<MeetingEntity, UUID> {

//    Optional<MeetingEntity> findByMeetingId(UUID meetingId);

    Optional<MeetingEntity> findByCode(String code);

    List<MeetingEntity> findByMeetingStatusAndConfirmedTimeBefore(MeetingStatus meetingStatus, LocalDateTime now);

    List<MeetingEntity> findByMeetingStatusAndDeadlineBefore(MeetingStatus meetingStatus, LocalDateTime now);
}
