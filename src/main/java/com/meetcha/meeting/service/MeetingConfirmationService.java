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
            Map<String, List<Integer>> alterTimes = AlternativeTimeCalculator.getAlternativeTimes(converted);
            boolean hasCandidate = !alterTimes.get("duration").isEmpty() || !alterTimes.get("participant").isEmpty();

            if (!hasCandidate) {
                meeting.setMeetingStatus(MeetingStatus.MATCH_FAILED);
            } else {
                saveAlternativeTimeCandidates(meetingId, alterTimes);
                updateAlternativeDeadlineFromCandidates(meeting);
                meeting.setMeetingStatus(MeetingStatus.MATCHING);
            }

            meetingRepository.save(meeting);
            return;
        }

        // 2. 알고리즘: 가장 많은 참여자가 가능한 시간대 추출
        LocalDateTime bestSlot = extractBestTime(allAvailability, meeting.getDurationMinutes());

        // 3. 확정 시간/상태 저장
        meeting.setConfirmedTime(bestSlot);
        meeting.setMeetingStatus(MeetingStatus.BEFORE);
        meetingRepository.save(meeting);

        // 4. 모든 참여자의 Google Calendar에 확정 시간 추가
        syncService.syncMeetingToCalendars(meeting);
    }

    private LocalDateTime extractBestTime(List<ParticipantAvailability> times, int durationMinutes) {
        // 1. 참여자별로 가능한 시간대 목록을 모음
        Map<UUID, List<TimeRange>> timeMap = new HashMap<>();
        for (ParticipantAvailability availability : times) {
            UUID userId = availability.getParticipantId();
            int start = toMinutes(availability.getStartAt());
            int end = toMinutes(availability.getEndAt());

            timeMap.computeIfAbsent(userId, k -> new ArrayList<>())
                    .add(new TimeRange(start, end));
        }

        // 2. Participant 리스트 생성
        List<Participant> participants = timeMap.entrySet().stream()
                .map(entry -> new Participant(entry.getKey().toString(), entry.getValue()))
                .collect(Collectors.toList());

        // 3. 후보 날짜 추출 (DayOfYear 기준)
        List<Integer> candidateDays = times.stream()
                .map(a -> a.getStartAt().getDayOfYear())
                .distinct()
                .collect(Collectors.toList());

        // 4. Meeting 객체 생성
        Meeting meeting = new Meeting(
                UUID.randomUUID().toString(), // 임시 ID
                participants,
                0, // deadline은 현재 사용하지 않음
                durationMinutes,
                null,
                null,
                new ArrayList<>(),
                0,
                candidateDays
        );

        // 5. 알고리즘 실행
        Integer bestTimeMinutes = MeetingTimeCalculator.calculateMeetingTime(meeting);
        if (bestTimeMinutes == null) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR); // 후보 없음
        }

        // 6. 분 단위 시간 → LocalDateTime 변환
        return convertToLocalDateTime(bestTimeMinutes);
    }

    private int toMinutes(LocalDateTime dateTime) {
        return dateTime.getDayOfYear() * 24 * 60 + dateTime.getHour() * 60 + dateTime.getMinute();
    }

    private LocalDateTime convertToLocalDateTime(int totalMinutes) {
        LocalDate baseDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        return baseDate.atStartOfDay().plusMinutes(totalMinutes);
    }


    private void saveAlternativeTimeCandidates(UUID meetingId, Map<String, List<Integer>> alterTimes) {
        List<Integer> allCandidates = new ArrayList<>();
        allCandidates.addAll(alterTimes.get("duration"));
        allCandidates.addAll(alterTimes.get("participant"));

        for (Integer minutes : allCandidates) {
            LocalDateTime startTime = convertToLocalDateTime(minutes);

            AlternativeTimeEntity entity = AlternativeTimeEntity.builder()
                    .meetingId(meetingId)
                    .startTime(startTime)
                    .build();

            alternativeTimeRepository.save(entity);
        }
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
