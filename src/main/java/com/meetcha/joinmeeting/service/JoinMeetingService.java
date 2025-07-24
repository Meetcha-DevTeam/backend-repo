package com.meetcha.joinmeeting.service;

import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.exception.InvalidJoinMeetingRequestException;
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

    @Transactional
    public JoinMeetingResponse join(UUID meetingId, JoinMeetingRequest request) {
        // todo 아직 SecurityContextHolder에 사용자정보 저장이 안되어있음 추후 추가하기
        UUID userId = getCurrentUserId();

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
        MeetingEntity meeting = meetingRepository.findByCode(code)
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

    @Transactional
    public JoinMeetingResponse updateParticipation(UUID meetingId, JoinMeetingRequest request) {
        // 1. 미팅 유효성 체크
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new InvalidJoinMeetingRequestException(ErrorCode.MEETING_NOT_FOUND));/// CustomException으로 통합

        if (meeting.isDeadlinePassed()) {
            throw new CustomException(ErrorCode.MEETING_DEADLINE_PASSED);
        }

        // todo 아직 SecurityContextHolder에 사용자정보 저장이 안되어있음 추후 추가하기
        UUID userId = getCurrentUserId();

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


    protected UUID getCurrentUserId() {
        // TODO: SecurityContextHolder구현 이후 실제 userId 추출
        return UUID.randomUUID(); // 예시용
    }


}

