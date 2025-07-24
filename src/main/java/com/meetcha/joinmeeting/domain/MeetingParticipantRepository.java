package com.meetcha.joinmeeting.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, UUID> {
    boolean existsByMeetingIdAndUserId(UUID meetingId, UUID userId);
    Optional<MeetingParticipant> findByMeeting_MeetingIdAndUserId(UUID meetingId, UUID userId);
}
