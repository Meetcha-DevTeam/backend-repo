package com.meetcha.meeting.service;

import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.joinmeeting.domain.MeetingParticipantRepository;
import com.meetcha.joinmeeting.domain.ParticipantAvailability;
import com.meetcha.joinmeeting.domain.ParticipantAvailabilityRepository;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.meeting.domain.MeetingStatus;
import com.meetcha.meeting.service.algorithm.*;
import com.meetcha.meetinglist.domain.AlternativeTimeEntity;
import com.meetcha.meetinglist.repository.AlternativeTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingConfirmationService {

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final ParticipantAvailabilityRepository availabilityRepository;
    private final MeetingScheduleSyncService syncService;
    private final AlternativeTimeRepository alternativeTimeRepository;

    /**
     * 미팅 확정 메서드
     * - 참여자들의 가용 시간 기반으로 최적 시간 계산
     * - 미팅에 확정 시간 저장
     * - 모든 참여자의 Google Calendar에 일정 추가
     */
    @Transactional
    public void confirmMeeting(UUID meetingId) {
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));

        // 1. 참여자 가용 시간 조회
        List<ParticipantAvailability> allAvailability = availabilityRepository.findByMeetingId(meetingId);
        if (allAvailability.isEmpty()) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR); //todo 후보가 없는 경우 예외 처리
        }

        // 변환 후 계산
        Meeting converted = MeetingConverter.toAlgorithmMeeting(meeting, allAvailability);
        Integer bestStartMinutes = MeetingTimeCalculator.calculateMeetingTime(converted);


        if (bestStartMinutes == null) {
            // 대안 시간 후보 산출 시도
            List<AlternativeTimeEntity> alterTimes = AlternativeTimeCalculator.getAlternativeTimes(converted,meetingId);
            boolean hasCandidate = !alterTimes.isEmpty();

            if (!hasCandidate) {
                // 이때 가용 시간이 없는 경우 미팅 상태 실패로 지정
                meeting.setMeetingStatus(MeetingStatus.MATCH_FAILED);
            } else {
                saveAlternativeTimeCandidates(alterTimes);
                updateAlternativeDeadlineFromCandidates(meeting);
                meeting.setMeetingStatus(MeetingStatus.MATCHING);
            }

            meetingRepository.save(meeting);
            return;
        }

        // 2. 알고리즘: 가장 많은 참여자가 가능한 시간대 추출
        LocalDateTime bestSlot = MeetingConverter.toLocalDateTime(bestStartMinutes);

        // 3. 확정 시간/상태 저장
        meeting.setConfirmedTime(bestSlot);
        meeting.setMeetingStatus(MeetingStatus.BEFORE);
        meetingRepository.save(meeting);

        // 4. 모든 참여자의 Google Calendar에 확정 시간 추가
        syncService.syncMeetingToCalendars(meeting);
    }

    private void saveAlternativeTimeCandidates(List<AlternativeTimeEntity> alterTimes) {
        alternativeTimeRepository.saveAll(alterTimes);
    }

    private void updateAlternativeDeadlineFromCandidates(MeetingEntity meeting) {
        List<AlternativeTimeEntity> candidates = alternativeTimeRepository.findByMeetingId(meeting.getMeetingId());

        if (candidates.isEmpty()) return;

        // 가장 이른 날짜의 전날 23:59
        LocalDate earliestDate = candidates.stream()
                .map(a -> a.getStartTime().toLocalDate())
                .min(LocalDate::compareTo)
                .orElseThrow();

        LocalDateTime alternativeDeadline = earliestDate.minusDays(1).atTime(23, 59);
        meeting.setAlternativeDeadline(alternativeDeadline); // MeetingEntity에 필드 존재해야 함
    }
}
