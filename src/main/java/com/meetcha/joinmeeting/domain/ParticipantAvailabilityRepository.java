package com.meetcha.joinmeeting.domain;

import com.meetcha.meetinglist.domain.ParticipantAvailabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ParticipantAvailabilityRepository extends JpaRepository<ParticipantAvailability, UUID> {
    void deleteByMeetingIdAndParticipantId(UUID meetingId, UUID participantId);

    List<ParticipantAvailability> findByMeetingId(UUID meetingId);

    List<ParticipantAvailability> findByMeetingIdAndParticipantId(UUID meetingId, UUID participantId);

    void deleteByMeetingId(UUID meetingId);

    List<ParticipantAvailabilityEntity> findAllByMeetingId(UUID meetingId);

}
