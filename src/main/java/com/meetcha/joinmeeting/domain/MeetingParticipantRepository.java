package com.meetcha.joinmeeting.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, UUID> {
    boolean existsByMeetingIdAndUserId(UUID meetingId, UUID userId);
    Optional<MeetingParticipant> findByMeetingIdAndUserId(UUID meetingId, UUID userId);

    List<MeetingParticipant> findAllByMeetingId(UUID meetingId);
}
