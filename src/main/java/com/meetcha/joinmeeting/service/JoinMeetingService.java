package com.meetcha.joinmeeting.service;

import com.meetcha.auth.domain.UserRepository;
import com.meetcha.auth.domain.UserEntity;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
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
        long startNs = System.nanoTime();
        int slotCount = request != null && request.getSelectedTimes() != null ? request.getSelectedTimes().size() : 0;
        boolean hasNickname = request != null && request.getNickname() != null && !request.getNickname().isBlank();

        log.info("[JOIN] start meetingId={} userId={} slotCount={} nicknameProvided={}",
                meetingId, userId, slotCount, hasNickname);

        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> {
                    log.warn("[JOIN] meeting not found meetingId={} userId={}", meetingId, userId);
                    return new CustomException(ErrorCode.MEETING_NOT_FOUND);
                });

        validateMeetingDeadLine(meeting, meetingId, userId, "JOIN");
        validateDuplicateParticipation(meetingId, userId, "JOIN");
        validateTimeSlot(request, meetingId, userId, "JOIN");

        String nickname = resolveNickname(request, userId, meetingId);
        MeetingParticipant participant = participantRepository.save(MeetingParticipant.create(userId, meeting, nickname));

        List<ParticipantAvailability> availabilities = convertTimeSlotsToAvailabilities(request, participant.getParticipantId(), meetingId);
        availabilityRepository.saveAll(availabilities);

        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        log.info("[JOIN] 미팅 참가 완료 meetingId = {} userId = {} participantId = {} slotCount={} elapsedMs={}",
                meetingId, userId, participant.getParticipantId(), availabilities.size(), elapsedMs);
        return new JoinMeetingResponse(meetingId, participant.getParticipantId());
    }

    private List<ParticipantAvailability> convertTimeSlotsToAvailabilities(JoinMeetingRequest request, UUID participant, UUID meetingId) {
        List<ParticipantAvailability> availabilities = request.getSelectedTimes().stream()
                .map(slot -> ParticipantAvailability.create(
                        participant,
                        meetingId,
                        DateTimeUtils.kstToUtc(slot.getStartAt()),
                        DateTimeUtils.kstToUtc(slot.getEndAt())
                ))
                .toList();
        return availabilities;
    }

    private String resolveNickname(JoinMeetingRequest request, UUID userId) {
        String nickname = request.getNickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = userRepository.findById(userId)
                    .map(UserEntity::getName)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        }
        return nickname;
    }

    private void validateTimeSlot(JoinMeetingRequest request) {
        for (JoinMeetingRequest.TimeSlot slot : request.getSelectedTimes()) {
            if (slot.getStartAt().isAfter(slot.getEndAt())) {
                throw new CustomException(ErrorCode.INVALID_TIME_SLOT);
            }
        }
    }

    private void validateDuplicateParticipation(UUID meetingId, UUID userId) {
        if (participantRepository.existsByMeeting_MeetingIdAndUserId(meetingId, userId)) {
            throw new CustomException(ErrorCode.ALREADY_JOINED_MEETING);
        }
    }

    private void validateMeetingDeadLine(MeetingEntity meeting) {
        if (meeting.isDeadlinePassed()) {
            throw new CustomException(ErrorCode.MEETING_DEADLINE_PASSED);
        }
    }

    //미팅코드 유효검사
    public ValidateMeetingCodeResponse validateMeetingCode(String code) {
        long startNs = System.nanoTime();
        log.info("[MEETING_CODE_VALIDATE] start code={}", safeCode(code));

        MeetingEntity meeting = meetingRepository.findByMeetingCode(code)
                .orElseThrow(() -> {
                    log.warn("[MEETING_CODE_VALIDATE] meeting not found code={}", safeCode(code));
                    return new CustomException(ErrorCode.MEETING_NOT_FOUND);
                });

        boolean isClosed = meeting.getDeadline().isBefore(LocalDateTime.now());

        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        log.info("[MEETING_CODE_VALIDATE] success meetingId={} closed={} elapsedMs={}",
                meeting.getMeetingId(), isClosed, elapsedMs);

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
        long startNs = System.nanoTime();
        log.info("[MEETING_INFO] start meetingId={}", meetingId);

        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> {
                    log.warn("[MEETING_INFO] meeting not found meetingId={}", meetingId);
                    return new CustomException(ErrorCode.MEETING_NOT_FOUND);
                });

        //candidate 조회
        List<LocalDate> candidateDates = meetingCandidateDateRepository
                .findAllByMeeting_MeetingId(meetingId)
                .stream()
                .map(MeetingCandidateDateEntity::getCandidateDate)
                .toList();

        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        log.info("[MEETING_INFO] success meetingId={} candidateDates={} elapsedMs={}",
                meetingId, candidateDates.size(), elapsedMs);

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

        long startNs = System.nanoTime();
        int slotCount = request != null && request.getSelectedTimes() != null ? request.getSelectedTimes().size() : 0;

        log.info("[PARTICIPATION_UPDATE] start meetingId={} userId={} slotCount={}",
                meetingId, userId, slotCount);

        // 1. 미팅 유효성 체크
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> {
                    log.warn("[PARTICIPATION_UPDATE] meeting not found meetingId={} userId={}", meetingId, userId);
                    return new CustomException(ErrorCode.MEETING_NOT_FOUND);
                });


        validateMeetingDeadLine(meeting, meetingId, userId, "PARTICIPATION_UPDATE");
        validateTimeSlot(request, meetingId, userId, "PARTICIPATION_UPDATE" );


        // 3. 기존 참여자 존재 확인
        MeetingParticipant participant = participantRepository
                .findByMeeting_MeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> {
                    log.warn("[PARTICIPATION_UPDATE] participant not found meetingId={} userId={}", meetingId, userId);
                    return new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND);
                });

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

        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        log.info("[PARTICIPATION_UPDATE] success meetingId={} userId={} participantId={} slotCount={} elapsedMs={}",
                meetingId, userId, participantId, availabilities.size(), elapsedMs);

        // 6. 응답 반환
        return new JoinMeetingResponse(meetingId, participantId);
    }

    @Transactional(readOnly = true)
    public List<GetSelectedTime> getMyAvailableTimes(UUID meetingId, UUID userId) {

        long startNs = System.nanoTime();
        log.info("[MY_AVAILABLE_TIMES] start meetingId={} userId={}", meetingId, userId);

        // userId로 participant 조회
        MeetingParticipant participant = participantRepository
                .findByMeeting_MeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> {
                    log.warn("[MY_AVAILABLE_TIMES] participant not found meetingId={} userId={}", meetingId, userId);
                    return new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND);
                });

        UUID participantId = participant.getParticipantId();

        // availability 조회
        List<ParticipantAvailability> times =
                availabilityRepository.findByMeetingIdAndParticipantId(meetingId, participantId);



        List<GetSelectedTime> result = times.stream()
                .map(t -> new GetSelectedTime(
                        DateTimeUtils.utcToKstString(t.getStartAt()),
                        DateTimeUtils.utcToKstString(t.getEndAt())
                ))
                .toList();

        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        log.info("[MY_AVAILABLE_TIMES] success meetingId={} userId={} participantId={} count={} elapsedMs={}",
                meetingId, userId, participantId, result.size(), elapsedMs);

        return result;
    }


}

