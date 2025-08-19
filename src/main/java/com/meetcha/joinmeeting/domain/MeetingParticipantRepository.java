package com.meetcha.joinmeeting.domain;

import com.meetcha.joinmeeting.dto.MeetingParticipantDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, UUID> {

    boolean existsByMeetingIdAndUserId(UUID meetingId, UUID userId);

    Optional<MeetingParticipant> findByMeetingIdAndUserId(UUID meetingId, UUID userId);

    List<MeetingParticipant> findAllByMeetingId(UUID meetingId);

    List<MeetingParticipant> findByUserId(UUID userId);

    @Query("""
       SELECT new com.meetcha.joinmeeting.dto.MeetingParticipantDto(
           p.participantId,
           p.nickname,
           u.profileImgSrc
       )
       FROM MeetingParticipant p
       JOIN UserEntity u ON u.userId = p.userId
       WHERE p.meeting.meetingId = :meetingId
       ORDER BY p.nickname ASC
    """)
    List<MeetingParticipantDto> findParticipantDtosByMeetingId(@Param("meetingId") UUID meetingId);

    @Query("SELECT p.nickname FROM MeetingParticipant p WHERE p.meeting.meetingId = :meetingId")
    List<String> findNicknamesByMeetingId(@Param("meetingId") UUID meetingId);
}

/*

@Repository
public interface ParticipantRepository extends JpaRepository<MeetingParticipant, UUID> {

    List<MeetingParticipant> findByMeeting_MeetingId(UUID meetingId);
    List<MeetingParticipant> findByUserId(UUID userId);
    @Query("""
   SELECT new com.meetcha.joinmeeting.dto.MeetingParticipantDto(
       p.participantId,
       p.nickname,
       u.profileImgSrc
   )
   FROM MeetingParticipant p
   JOIN UserEntity u ON u.userId = p.userId
   WHERE p.meetingId = :meetingId
   ORDER BY p.nickname ASC
""")
    List<MeetingParticipantDto> findParticipantDtosByMeetingId(@Param("meetingId") UUID meetingId);

    @Query("SELECT p.nickname FROM MeetingParticipant p WHERE p.meetingId = :meetingId")
    List<String> findNicknamesByMeetingId(@Param("meetingId") UUID meetingId);
}
*/
