package com.meetcha.meeting.service;

import com.meetcha.global.util.DateTimeUtils;
import com.meetcha.joinmeeting.domain.ParticipantAvailability;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.service.algorithm.Meeting;
import com.meetcha.meeting.service.algorithm.Participant;
import com.meetcha.meeting.service.algorithm.TimeRange;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
        import java.util.stream.Collectors;
/**
 * 역할:
 * - MeetingEntity, ParticipantAvailability 등 도메인 객체를
 *   알고리즘용 POJO (Meeting, Participant, TimeRange)로 변환
 * - 알고리즘 결과(분 단위 시간)를 LocalDateTime으로 변환하는 기능 제공
 *
 * 사용 목적:
 * - 도메인 모델과 알고리즘 모델의 관심사를 분리
 * - 알고리즘을 독립적으로 테스트하거나 외부 서비스로 분리하기 위함
 *
 *
 * - 시간은 분 단위로 변환되며, 기준일은 현재 연도 1월 1일
 */

public class MeetingConverter {

    public static Meeting toAlgorithmMeeting(MeetingEntity entity, List<ParticipantAvailability> availabilities) {
        Map<UUID, List<TimeRange>> timeMap = new HashMap<>();
        for (ParticipantAvailability a : availabilities) {
            UUID userId = a.getParticipantId();
            int start = toMinutes(a.getStartAt());
            int end = toMinutes(a.getEndAt());
            timeMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(new TimeRange(start, end));
        }

        List<Participant> participants = timeMap.entrySet().stream()
                .map(e -> new Participant(e.getKey().toString(), e.getValue()))
                .collect(Collectors.toList());

        List<Integer> candidateDays = availabilities.stream()
                .map(a -> a.getStartAt().getDayOfYear())
                .distinct()
                .collect(Collectors.toList());

        return new Meeting(
                entity.getMeetingId().toString(),
                participants,
                0,
                entity.getDurationMinutes(),
                null,
                null,
                new ArrayList<>(),
                0,
                candidateDays
        );
    }

    private static int toMinutes(LocalDateTime timeUtc) {
        LocalDateTime timeKst = DateTimeUtils.utcToKst(timeUtc);
        return timeKst.getDayOfYear() * 24 * 60 + timeKst.getHour() * 60 + timeKst.getMinute();
    }

    public static LocalDateTime toLocalDateTime(int totalMinutes) {
        LocalDate baseDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        LocalDateTime kstTime = baseDate.atStartOfDay().plusMinutes(totalMinutes);

        // 다시 DB 저장용으로는 UTC 변환
        return DateTimeUtils.kstToUtc(kstTime);
    }
}
