package com.meetcha.global.util;

import com.meetcha.auth.domain.RefreshTokenEntity;
import com.meetcha.auth.domain.RefreshTokenRepository;
import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.joinmeeting.domain.MeetingParticipant;
import com.meetcha.joinmeeting.domain.MeetingParticipantRepository;
import com.meetcha.joinmeeting.domain.ParticipantAvailability;
import com.meetcha.joinmeeting.domain.ParticipantAvailabilityRepository;
import com.meetcha.meeting.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class TestDataFactory {
    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MeetingParticipantRepository meetingParticipantRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private ParticipantAvailabilityRepository participantAvailabilityRepository;

    public UserEntity createUser(String email) {
        LocalDateTime now = LocalDateTime.now();
        UserEntity user = new UserEntity(null, "사용자1", email, "existing google token", now, "existing profile image", "existing refresh token", now.plusDays(3));
        return userRepository.save(user);
    }

    public MeetingEntity createMeeting(UUID userId, LocalDateTime createdAt, LocalDateTime deadline) {
        List<LocalDate> candDates = List.of(createdAt.plusDays(5).toLocalDate(), createdAt.plusDays(6).toLocalDate());

        MeetingEntity meeting = MeetingEntity.builder()
                .title("미팅1")
                .description("미팅1입니다")
                .durationMinutes(60)
                .deadline(deadline)
                .createdAt(createdAt)
                .meetingStatus(MeetingStatus.MATCHING)
                .confirmedTime(null)
                .createdBy(userId)
                .meetingCode(UUID.randomUUID().toString().substring(0, 8))
                .build();

        List<MeetingCandidateDateEntity> candidateDates = candDates.stream()
                .map(date -> new MeetingCandidateDateEntity(meeting, date))
                .toList();

        meeting.setCandidateDates(candidateDates);

        return meetingRepository.save(meeting);
    }

    public MeetingParticipant createMeetingParticipant(String nickname, UUID userId, UUID meetingId) {
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 미팅입니다."));
        MeetingParticipant meetingParticipant = MeetingParticipant.create(userId, meeting, nickname);
        return meetingParticipantRepository.save(meetingParticipant);
    }

    public RefreshTokenEntity saveRefreshToken(UUID userId, String refreshToken, LocalDateTime expireAt) {
        RefreshTokenEntity refresh = new RefreshTokenEntity(userId, refreshToken, expireAt);
        return refreshTokenRepository.save(refresh);
    }

    public ParticipantAvailability createParticipantAvailability(UUID participantId, UUID meetingId, LocalDateTime startAt, LocalDateTime endAt
    ) {
        ParticipantAvailability availability =
                ParticipantAvailability.create(
                        participantId,
                        meetingId,
                        startAt,
                        endAt
                );

        return participantAvailabilityRepository.save(availability);
    }

}
