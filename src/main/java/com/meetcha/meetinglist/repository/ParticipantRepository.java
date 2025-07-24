package com.meetcha.meetinglist.repository;

import com.meetcha.meetinglist.domain.ParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParticipantRepository extends JpaRepository<ParticipantEntity, UUID> {

    List<ParticipantEntity> findByMeetingId(UUID meetingId);
}
