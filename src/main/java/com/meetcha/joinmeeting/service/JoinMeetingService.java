package com.meetcha.joinmeeting.service;

import com.meetcha.auth.domain.UserRepository;
import com.meetcha.auth.domain.UserEntity;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.util.AuthHeaderUtils;
import com.meetcha.global.util.DateTimeUtils;
import com.meetcha.joinmeeting.domain.MeetingParticipant;
import com.meetcha.joinmeeting.domain.MeetingParticipantRepository;
import com.meetcha.joinmeeting.domain.ParticipantAvailability;
import com.meetcha.joinmeeting.domain.ParticipantAvailabilityRepository;
import com.meetcha.joinmeeting.dto.GetSelectedTime;
import com.meetcha.joinmeeting.dto.JoinMeetingRequest;
import com.meetcha.joinmeeting.dto.JoinMeetingResponse;
import com.meetcha.joinmeeting.dto.ValidateMeetingCodeResponse;
import com.meetcha.meeting.domain.MeetingCandidateDateEntity;
import com.meetcha.meeting.domain.MeetingCandidateDateRepository;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.meeting.dto.MeetingInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JoinMeetingService {

    private final MeetingParticipantRepository participantRepository;
    private final ParticipantAvailabilityRepository availabilityRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingCandidateDateRepository meetingCandidateDateRepository;
    private final UserRepository userRepository;

    @Transactional
    public JoinMeetingResponse join(UUID meetingId, JoinMeetingRequest request, UUID userId) {

        log.debug("join 메서드 진입");
        // 미팅 조회
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));

        // 마감 시간 확인
        if (meeting.isDeadlinePassed()) {
            throw new CustomException(ErrorCode.MEETING_DEADLINE_PASSED);
        }

        // 중복 참가 방지
        if (participantRepository.existsByMeeting_MeetingIdAndUserId(meetingId, userId)) {
            throw new CustomException(ErrorCode.ALREADY_JOINED_MEETING);
        }


        for (JoinMeetingRequest.TimeSlot slot : request.getSelectedTimes()) {
            if (slot.getStartAt().isAfter(slot.getEndAt())) {
                throw new CustomException(ErrorCode.INVALID_TIME_SLOT);
            }
        }


        // 닉네임 확인 (없으면 users.name 가져오기)
        String nickname = request.getNickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = userRepository.findById(userId)
                    .map(UserEntity::getName)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        }

        // 참가자 저장
        MeetingParticipant participant = MeetingParticipant.create(
                userId,
                meeting,
                nickname
        );
        participantRepository.save(participant);

        //  선택 시간 저장
        List<ParticipantAvailability> availabilities = request.getSelectedTimes().stream()
                .map(slot -> ParticipantAvailability.create(
                        participant.getParticipantId(),
                        meetingId,
                        DateTimeUtils.kstToUtc(slot.getStartAt()),
                        DateTimeUtils.kstToUtc(slot.getEndAt())
                ))
                .toList();

        availabilityRepository.saveAll(availabilities);

        // 응답 반환
        return new JoinMeetingResponse(meetingId, participant.getParticipantId());
    }

    //미팅코드 유효검사
    public ValidateMeetingCodeResponse validateMeetingCode(String code) {
        MeetingEntity meeting = meetingRepository.findByMeetingCode(code)
                .orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));

        boolean isClosed = meeting.getDeadline().isBefore(LocalDateTime.now());

        return new ValidateMeetingCodeResponse(
                meeting.getMeetingId(),
                meeting.getTitle(),
                meeting.getDescription(),
                DateTimeUtils.utcToKst(meeting.getDeadline()),
                isClosed
        );
    }


    //미팅 참여, 미팅정보확인 시 사용
    public MeetingInfoResponse getMeetingInfo(UUID meetingId) {
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));

        //candidate 조회
        List<LocalDate> candidateDates = meetingCandidateDateRepository
                .findAllByMeeting_MeetingId(meetingId)
                .stream()
                .map(MeetingCandidateDateEntity::getCandidateDate)
                .toList();

        return new MeetingInfoResponse(
                meeting.getMeetingId(),
                meeting.getTitle(),
                meeting.getDescription(),
                meeting.getDurationMinutes(),
                candidateDates,
                DateTimeUtils.utcToKst(meeting.getDeadline()),
                DateTimeUtils.utcToKst(meeting.getCreatedAt())
        );
    }


    //미팅정보 수정 로직
    @Transactional
    public JoinMeetingResponse updateParticipation(UUID meetingId, JoinMeetingRequest request, UUID userId) {
        // 1. 미팅 유효성 체크
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));

        if (meeting.isDeadlinePassed()) {
            throw new CustomException(ErrorCode.MEETING_DEADLINE_PASSED);
        }


        // 3. 기존 참여자 존재 확인
        MeetingParticipant participant = participantRepository
                .findByMeeting_MeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND));

        UUID participantId = participant.getParticipantId();

        // 4. 기존 availability 삭제
        availabilityRepository.deleteByMeetingIdAndParticipantId(meetingId, participantId);

        // 5. 새 availability 저장
        List<ParticipantAvailability> availabilities = request.getSelectedTimes().stream()
                .map(slot -> ParticipantAvailability.create(
                        participantId,
                        meetingId,
                        DateTimeUtils.kstToUtc(slot.getStartAt()),
                        DateTimeUtils.kstToUtc(slot.getEndAt())
                ))
                .toList();

        availabilityRepository.saveAll(availabilities);

        // 6. 응답 반환
        return new JoinMeetingResponse(meetingId, participantId);
    }

    @Transactional(readOnly = true)
    public List<GetSelectedTime> getMyAvailableTimes(UUID meetingId, UUID userId) {
        // userId로 participant 조회
        MeetingParticipant participant = participantRepository
                .findByMeeting_MeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND));

        UUID participantId = participant.getParticipantId();

        // availability 조회
        List<ParticipantAvailability> times =
                availabilityRepository.findByMeetingIdAndParticipantId(meetingId, participantId);



        return times.stream()
                .map(t -> new GetSelectedTime(
                        DateTimeUtils.utcToKstString(t.getStartAt()),
                        DateTimeUtils.utcToKstString(t.getEndAt())
                ))
                .toList();
    }


}

