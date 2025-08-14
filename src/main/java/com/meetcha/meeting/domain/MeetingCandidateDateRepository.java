package com.meetcha.meeting.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MeetingCandidateDateRepository extends JpaRepository<MeetingCandidateDateEntity, UUID> {
    List<MeetingCandidateDateEntity> findAllByMeeting_MeetingId(UUID meetingId);
}