package com.meetcha.joinmeeting.service;

import com.meetcha.global.dto.ApiResponse;
import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.exception.InvalidJoinMeetingRequestException;
import com.meetcha.global.util.AuthHeaderUtils;
import com.meetcha.global.util.DateTimeUtils;
import com.meetcha.joinmeeting.domain.MeetingParticipant;
import com.meetcha.joinmeeting.domain.MeetingParticipantRepository;
import com.meetcha.joinmeeting.domain.ParticipantAvailability;
import com.meetcha.joinmeeting.domain.ParticipantAvailabilityRepository;
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
    private final JwtProvider jwtProvider;
    private final MeetingCandidateDateRepository meetingCandidateDateRepository;

    @Transactional
    public JoinMeetingResponse join(UUID meetingId, JoinMeetingRequest request, String authorizationHeader) {
        UUID userId = extractUserId(authorizationHeader);

        log.debug("join 메서드 진입");
        // 미팅 조회
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));

        // 마감 시간 확인
        if (meeting.isDeadlinePassed()) {
            throw new CustomException(ErrorCode.MEETING_DEADLINE_PASSED);
        }

        // 중복 참가 방지
        if (participantRepository.existsByMeetingIdAndUserId(meetingId, userId)) {
            throw new CustomException(ErrorCode.ALREADY_JOINED_MEETING);
        }

        // 참가자 저장
        MeetingParticipant participant = MeetingParticipant.create(
                userId,
                meeting,
                request.nickname()
        );
        participantRepository.save(participant);

        //  선택 시간 저장
        List<ParticipantAvailability> availabilities = request.selectedTimes().stream()
                .map(slot -> ParticipantAvailability.create(
                        participant.getParticipantId(),
                        meetingId,
                        DateTimeUtils.kstToUtc(slot.startAt()),
                        DateTimeUtils.kstToUtc(slot.endAt())
                ))
                .toList();

        availabilityRepository.saveAll(availabilities);

        // 응답 반환
        return new JoinMeetingResponse(meetingId, participant.getParticipantId());
    }

    //미팅코드 유효검사
    public ApiResponse<ValidateMeetingCodeResponse> validateMeetingCode(String code) {
        MeetingEntity meeting = meetingRepository.findByMeetingCode(code)
                .orElseThrow(() -> new InvalidJoinMeetingRequestException(ErrorCode.MEETING_NOT_FOUND));

        boolean isClosed = meeting.getDeadline().isBefore(LocalDateTime.now());



        var body = new ValidateMeetingCodeResponse(
                meeting.getMeetingId(),
                meeting.getTitle(),
                meeting.getDescription(),
                DateTimeUtils.utcToKst(meeting.getDeadline()),
                isClosed
        );

        return ApiResponse.success(200, "유효한 미팅입니다.", body);
    }


    //미팅 참여, 미팅정보확인 시 사용
    public MeetingInfoResponse getMeetingInfo(UUID meetingId) {
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new InvalidJoinMeetingRequestException(ErrorCode.MEETING_NOT_FOUND));

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
    public JoinMeetingResponse updateParticipation(UUID meetingId, JoinMeetingRequest request, String authorizationHeader) {
        // 1. 미팅 유효성 체크
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new InvalidJoinMeetingRequestException(ErrorCode.MEETING_NOT_FOUND));

        if (meeting.isDeadlinePassed()) {
            throw new CustomException(ErrorCode.MEETING_DEADLINE_PASSED);
        }

        UUID userId = extractUserId(authorizationHeader);


        // 3. 기존 참여자 존재 확인
        MeetingParticipant participant = participantRepository
                .findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND));

        UUID participantId = participant.getParticipantId();

        // 4. 기존 availability 삭제
        availabilityRepository.deleteByMeetingIdAndParticipantId(meetingId, participantId);

        // 5. 새 availability 저장
        List<ParticipantAvailability> availabilities = request.selectedTimes().stream()
                .map(slot -> ParticipantAvailability.create(
                        participantId,
                        meetingId,
                        DateTimeUtils.kstStringToUtc(slot.startAt().toString()),
                        DateTimeUtils.kstStringToUtc(slot.endAt().toString())
                ))
                .toList();

        availabilityRepository.saveAll(availabilities);

        // 6. 응답 반환
        return new JoinMeetingResponse(meetingId, participantId);
    }


    private UUID extractUserId(String authorizationHeader) {
        String token = AuthHeaderUtils.extractBearerToken(authorizationHeader);
        if (!jwtProvider.validateToken(token)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }
        return jwtProvider.getUserId(token);
    }
}

