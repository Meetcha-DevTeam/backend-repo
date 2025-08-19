package com.meetcha.meetinglist.repository;

import com.meetcha.joinmeeting.domain.MeetingParticipant;
import com.meetcha.joinmeeting.dto.MeetingParticipantDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParticipantRepository extends JpaRepository<MeetingParticipant, UUID> {

    List<MeetingParticipant> findByMeeting_MeetingId(UUID meetingId);
    List<MeetingParticipant> findByUserId(UUID userId);
    @Query("""
       SELECT new com.meetcha.joinmeeting.dto.MeetingParticipantDto(
           p.participantId,
           p.nickname,
           u.profileImageUrl
       )
       FROM MeetingParticipant p
       JOIN UserEntity u ON u.userId = p.userId      -- Hibernate 6 OK
       WHERE p.meetingId = :meetingId
       ORDER BY p.nickname ASC
    """)
    List<MeetingParticipantDto> findParticipantDtosByMeetingId(@Param("meetingId") UUID meetingId);

    @Query("SELECT p.nickname FROM MeetingParticipant p WHERE p.meeting.meetingId = :meetingId")
    List<String> findNicknamesByMeetingId(@Param("meetingId") UUID meetingId);
}
