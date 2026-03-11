package com.meetcha.joinmeeting.domain;

import com.meetcha.joinmeeting.dto.MeetingParticipantDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, UUID> {

    boolean existsByMeeting_MeetingIdAndUserId(UUID meetingId, UUID userId);

    Optional<MeetingParticipant> findByMeeting_MeetingIdAndUserId(UUID meetingId, UUID userId);

    List<MeetingParticipant> findAllByMeeting_MeetingId(UUID meetingId);

    @Query("select p.participantId from MeetingParticipant p where p.meeting.meetingId = :meetingId")
    List<UUID> findParticipantIdsByMeetingId(@Param("meetingId") UUID meetingId);

    @Query("""
       SELECT new com.meetcha.joinmeeting.dto.MeetingParticipantDto(
           p.participantId,
           COALESCE(p.nickname, u.name),
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

    void deleteByMeeting_MeetingId(UUID meetingId);

<<<<<<< HEAD
    int countByMeeting_MeetingId(UUID meetingId);
}
=======
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
>>>>>>> a2d63f45550912fbc5ebd94824edd49ab1746bb9
