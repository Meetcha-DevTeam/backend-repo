package com.meetcha.joinmeeting.service;

import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.exception.InvalidJoinMeetingRequestException;
import com.meetcha.global.util.AuthHeaderUtils;
import com.meetcha.joinmeeting.domain.MeetingParticipant;
import com.meetcha.joinmeeting.domain.MeetingParticipantRepository;
import com.meetcha.joinmeeting.domain.ParticipantAvailability;
import com.meetcha.joinmeeting.domain.ParticipantAvailabilityRepository;
import com.meetcha.joinmeeting.dto.JoinMeetingRequest;
import com.meetcha.joinmeeting.dto.JoinMeetingResponse;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.meeting.dto.MeetingInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JoinMeetingService {

    private final MeetingParticipantRepository participantRepository;
    private final ParticipantAvailabilityRepository availabilityRepository;
    private final MeetingRepository meetingRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public JoinMeetingResponse join(UUID meetingId, JoinMeetingRequest request,  String authorizationHeader) {
        UUID userId = extractUserId(authorizationHeader);

        // 중복 참가 방지
        if (participantRepository.existsByMeetingIdAndUserId(meetingId, userId)) {
            throw new RuntimeException("중복 참가");
        }

        // 참가자 저장
        MeetingParticipant participant = MeetingParticipant.create(
                userId,
                meetingId,
                request.nickname()
        );
        participantRepository.save(participant);

        //  선택 시간 저장
        List<ParticipantAvailability> availabilities = request.selectedTimes().stream()
                .map(slot -> ParticipantAvailability.create(
                        participant.getParticipantId(),
                        meetingId,
                        slot.startAt(),
                        slot.endAt()
                ))
                .toList();

        availabilityRepository.saveAll(availabilities);

        // 응답 반환
        return new JoinMeetingResponse(meetingId, participant.getParticipantId());
    }

    public void validateMeetingCode(String code) {
        MeetingEntity meeting = meetingRepository.findByMeetingCode(code)
                .orElseThrow(() -> new InvalidJoinMeetingRequestException(ErrorCode.MEETING_NOT_FOUND));

        if (meeting.getDeadline().isBefore(LocalDateTime.now())) {
            throw new InvalidJoinMeetingRequestException(ErrorCode.MEETING_DEADLINE_PASSED);
        }
    }


    //미팅 참여, 미팅정보확인 시 사용
    public MeetingInfoResponse getMeetingInfo(UUID meetingId) {
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new InvalidJoinMeetingRequestException(ErrorCode.MEETING_NOT_FOUND));

        return new MeetingInfoResponse(
                meeting.getMeetingId(),
                meeting.getTitle(),
                meeting.getDescription(),
                meeting.getMeetingStatus(),
                meeting.getDeadline(),
                meeting.getDurationMinutes(),
                meeting.getConfirmedTime()
        );
    }


    //미팅정보 수정 로직
    @Transactional
    public JoinMeetingResponse updateParticipation(UUID meetingId, JoinMeetingRequest request, String authorizationHeader) {
        // 1. 미팅 유효성 체크
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new InvalidJoinMeetingRequestException(ErrorCode.MEETING_NOT_FOUND));/// CustomException으로 통합

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
                        slot.startAt(),
                        slot.endAt()
                ))
                .toList();

        availabilityRepository.saveAll(availabilities);

        // 6. 응답 반환
        return new JoinMeetingResponse(meetingId, participantId);
    }

/*
    //테스트용
@Transactional
public JoinMeetingResponse updateParticipation(UUID meetingId, JoinMeetingRequest request) {
    // 1. meetingId null 이면 고정값 설정 (테스트용)
    if (meetingId == null) {
        meetingId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    }

    // 2. 더미 사용자 ID
    UUID userId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    UUID participantId = UUID.randomUUID();

    // 3. 요청 값 확인 로그

    if (request != null && request.selectedTimes() != null) {
        request.selectedTimes().forEach(slot -> {
            System.out.println("선택된 시간 슬롯: " + slot.startAt() + " ~ " + slot.endAt());
        });
    } else {
        System.out.println("선택된 시간이 없습니다.");
    }

    // 4. DB 저장 없이 바로 응답 반환
    return new JoinMeetingResponse(meetingId, participantId);
}
*/


    private UUID extractUserId(String authorizationHeader) {
        String token = AuthHeaderUtils.extractBearerToken(authorizationHeader);
        if (!jwtProvider.validateToken(token)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }
        return jwtProvider.getUserId(token);
    }
}

