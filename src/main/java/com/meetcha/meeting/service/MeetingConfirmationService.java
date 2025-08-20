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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
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
        log.info("confirmMeeting 접근 완료");
        // 1. 참여자 가용 시간 조회
        List<ParticipantAvailability> allAvailability = availabilityRepository.findByMeetingId(meetingId);
        if (allAvailability.isEmpty()) {
            throw new CustomException(ErrorCode.NO_PARTICIPANT_AVAILABILITY);
        }
        log.info("참여자 가용 시간 조회 완료");

        // 변환 후 계산
        Meeting converted = MeetingConverter.toAlgorithmMeeting(meeting, allAvailability);
        Integer bestStartMinutes = MeetingTimeCalculator.calculateMeetingTime(converted);
        log.info("변환 후 계산 완료");


        if (bestStartMinutes == null) {
            // 대안 시간 후보 산출 시도
            log.info("대안 시간 후보 산출 시도 {}",bestStartMinutes);
            List<AlternativeTimeEntity> alterTimes = AlternativeTimeCalculator.getAlternativeTimes(converted,meetingId);
            boolean hasCandidate = !alterTimes.isEmpty();
            log.info("대안 시간 후보 산출 완료 {}",hasCandidate);

            if (!hasCandidate) {
                // 이때 가용 시간이 없는 경우 미팅 상태 실패로 지정
                meeting.setMeetingStatus(MeetingStatus.MATCH_FAILED);
                log.info("가용 시간이 없는 경우 미팅 상태 실패로 지정");

            } else {
                log.info("saveAlternativeTimeCandidates 호출");
                saveAlternativeTimeCandidates(alterTimes);
//                updateAlternativeDeadlineFromCandidates(meeting);
                updateAlternativeDeadlineFromCandidates(meeting, alterTimes);
                meeting.setMeetingStatus(MeetingStatus.MATCHING);
                log.info("MATCHING 상태로 설정");

            }

            meetingRepository.save(meeting);
            log.info("meetingRepository.save 완료");

            return;
        }

        // 2. 알고리즘: 가장 많은 참여자가 가능한 시간대 추출
        LocalDateTime bestSlot = MeetingConverter.toLocalDateTime(bestStartMinutes);
        log.info(" 알고리즘: 가장 많은 참여자가 가능한 시간대 추출 완료{}",bestSlot);



        // 3. 확정 시간/상태 저장
        meeting.setConfirmedTime(bestSlot);
        meeting.setMeetingStatus(MeetingStatus.BEFORE);
        meetingRepository.save(meeting);
        log.info(" . 확정 시간/상태 저장");

        // 4. 모든 참여자의 Google Calendar에 확정 시간 추가
        syncService.syncMeetingToCalendars(meeting);
        log.info("  Google Calendar에 확정 시간");

    }

    private void saveAlternativeTimeCandidates(List<AlternativeTimeEntity> alterTimes) {
        log.info("saveAlternativeTimeCandidates 접근 완료");
        log.info("alterTimes size={}", alterTimes.size());
        for (AlternativeTimeEntity t : alterTimes) {
            log.info("cand start={} end={} meetingId={} excluded.len={}",
                    t.getStartTime(), t.getEndTime(), t.getMeetingId(),
                    t.getExcludedParticipants() == null ? 0 : t.getExcludedParticipants().length());
        }

        alternativeTimeRepository.saveAll(alterTimes);
        alternativeTimeRepository.flush();
        log.info("alternativeTimeRepository.saveAll OK");
    }

    // 기존 candidates 바로 갖다 쓰기
    private void updateAlternativeDeadlineFromCandidates(MeetingEntity meeting,
                                                         List<AlternativeTimeEntity> candidates) {
        if (candidates == null || candidates.isEmpty()) return;

        LocalDate earliestDate = candidates.stream()
                .map(a -> a.getStartTime().toLocalDate())
                .min(LocalDate::compareTo)
                .orElseThrow();

        LocalDateTime alternativeDeadline = earliestDate.minusDays(1).atTime(23, 59);
        meeting.setAlternativeDeadline(alternativeDeadline);
    }

    // DB 재조회 버전
 /*   private void updateAlternativeDeadlineFromCandidates(MeetingEntity meeting) {
        List<AlternativeTimeEntity> candidates = alternativeTimeRepository.findByMeetingId(meeting.getMeetingId());

        if (candidates.isEmpty()) return;

        // 가장 이른 날짜의 전날 23:59
        LocalDate earliestDate = candidates.stream()
                .map(a -> a.getStartTime().toLocalDate())
                .min(LocalDate::compareTo)
                .orElseThrow();

        LocalDateTime alternativeDeadline = earliestDate.minusDays(1).atTime(23, 59);
        meeting.setAlternativeDeadline(alternativeDeadline); // MeetingEntity에 필드 존재해야 함
    }*/
}
