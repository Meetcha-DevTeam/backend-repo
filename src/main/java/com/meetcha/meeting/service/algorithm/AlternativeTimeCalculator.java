package com.meetcha.meeting.service.algorithm;

import com.meetcha.meetinglist.domain.AlternativeTimeEntity;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 대안 시간 산출 알고리즘
 *
 * - 1차 알고리즘 실패 시 fallback 용도로 사용
 * - 현재 전략:
 *   1. 진행 시간을 줄이되, 모든 참여자가 동시에 참석 가능한 시간대만 후보로 사용
 */
@Slf4j
public class AlternativeTimeCalculator {

    private static final int PER = 30;

    /**
     * 대안 시간 후보를 계산
     *
     * @param meeting 미팅 정보
     * @return Map<"duration", 후보들>, Map<"participant", 후보들>
     */
    public static List<AlternativeTimeEntity> getAlternativeTimes(Meeting meeting, UUID meetingId) {
        List<AlternativeTimeEntity> results = new ArrayList<>();
        log.info("getAlternativeTimes 진입 {}",results);

        // 전략 1: 진행 시간 줄이기
        Map<Integer, Integer> timeSequenceDuration = getTimeSequence(meeting, PER, (cur, tot) -> cur == tot);
        if (!timeSequenceDuration.isEmpty()) {
            int maxHit = timeSequenceDuration.values().stream().max(Integer::compareTo).orElse(0);
            int adjustedDuration = maxHit * PER; // 줄어든 소요 시간

            for (Integer minutes : getLessDurationMeetingTimes(meeting)) {
                LocalDateTime start = toLocalDateTime(minutes);
                results.add(AlternativeTimeEntity.builder()
                        .alternativeTimeId(UUID.randomUUID())
                        .meetingId(meetingId)
                        .startTime(start)
                        .endTime(start.plusMinutes(adjustedDuration))
                        .durationAdjustedMinutes(adjustedDuration)
                        .excludedParticipants(null) // 전략1은 제외 인원 없음
                        .build());
            }
        }

        /*
        // 전략 2: 참여자 줄이기
        int total = meeting.getParticipants().size();
        int left = (int) Math.ceil((2.0 * total) / 3);
        int right = total;
        int maxParticipants = left;

        while (left <= right) {
            int mid = (left + right) / 2;
            if (!getTimeSequence(meeting, PER, (cur, t) -> cur >= mid).isEmpty()) {
                maxParticipants = Math.max(maxParticipants, mid);
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        final int fixedMaxParticipants = maxParticipants;

        Map<Integer, List<String>> timeBlockMap = TimeUtils.flattenParticipantTimes(meeting, PER);
        List<String> allParticipants = meeting.getParticipants().stream()
                .map(Participant::getId)
                .collect(Collectors.toList());

        // 단순히 참가자 수로 제외된 인원 계산
        List<String> excluded = allParticipants.size() > fixedMaxParticipants
                ? allParticipants.subList(fixedMaxParticipants, allParticipants.size())
                : Collections.emptyList();

        int adjustedDuration2 = meeting.getDuration(); // 시간은 그대로, 인원만 줄임
        for (Integer minutes : getLessParticipantMeetingTimes(meeting)) {
            LocalDateTime start = toLocalDateTime(minutes);
            results.add(AlternativeTimeEntity.builder()
//                    .alternativeTimeId(UUID.randomUUID())
                    .meetingId(meetingId)
                    .startTime(start)
                    .endTime(start.plusMinutes(adjustedDuration2))
                    .durationAdjustedMinutes(adjustedDuration2)
                    .excludedParticipants(String.join(",", excluded))
                    .build());
        }
*/
        return results;
    }

    private static LocalDateTime toLocalDateTime(int totalMinutes) {
        LocalDate baseDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        return baseDate.atStartOfDay().plusMinutes(totalMinutes);
    }

    /**
     * [전략 1] 진행 시간을 줄여 가능한 시간 후보를 찾는다.
     */
    private static List<Integer> getLessDurationMeetingTimes(Meeting meeting) {
        Map<Integer, Integer> timeSequence = getTimeSequence(
                meeting,
                PER,
                (cur, tot) -> cur == tot
        );

        if (timeSequence.isEmpty()) return Collections.emptyList();

        int maxHit = timeSequence.values().stream()
                .max(Integer::compareTo)
                .orElse(0);

        // 2/3 이상 진행 시간 확보 안되면 대안 없음
        int requiredHit = (int) Math.ceil((2.0 * meeting.getDuration() / 3) / PER);
        if (maxHit < requiredHit) return Collections.emptyList();

        List<Integer> timeList = timeSequence.keySet().stream()
                .sorted(Comparator.comparingInt(timeSequence::get))
                .collect(Collectors.toList());
        // ‘가운데로 당길 기준(hit)’은 실제 회의 길이(블록)과 maxHit 중 작은 값이어야 함
        //       (기존 코드는 maxHit를 그대로 넘겨 가운데가 0으로 계산되는 문제가 있었음)
        int hit = Math.min(meeting.getDuration() / PER, maxHit);

        // SortUtils.sortBySpare 시그니처 변경에 맞춰 seqMap 전달
        List<Integer> spareCandidates = SortUtils.sortBySpare(timeList, timeSequence, hit, PER);
        if (spareCandidates.isEmpty()) return Collections.emptyList();

        List<Integer> earlyCandidates = SortUtils.sortByDay(spareCandidates);
        if (earlyCandidates.isEmpty()) return Collections.emptyList();

        return SortUtils.sortByTimePriority(earlyCandidates);
    }

    /*
     * [전략 2] 참여자를 줄여 가능한 시간 후보를 찾는다.

    private static List<Integer> getLessParticipantMeetingTimes(Meeting meeting) {
        int total = meeting.getParticipants().size();
        int left = (int) Math.ceil((2.0 * total) / 3);
        int right = total;
        int maxParticipants = left;

        // 이분 탐색으로 가능한 최대 참여자 수 계산
        while (left <= right) {
            int mid = (left + right) / 2;
            Map<Integer, Integer> timeSequence = getTimeSequence(meeting, PER, (cur, t) -> cur >= mid);
            if (!timeSequence.isEmpty()) {
                maxParticipants = Math.max(maxParticipants, mid);
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        // 람다에서 사용하는 변수는 final 이어야 하므로 복사본 생성
        final int fixedMaxParticipants = maxParticipants;

        // 최대 참여자 수 기준으로 재계산
        Map<Integer, Integer> validSequence = getTimeSequence(meeting, PER, (cur, t) -> cur == fixedMaxParticipants);
        if (validSequence.isEmpty()) return Collections.emptyList();

        List<Integer> timeList = validSequence.keySet().stream()
                .sorted(Comparator.comparingInt(validSequence::get))
                .collect(Collectors.toList());
        if (timeList.isEmpty()) return Collections.emptyList();

        int hit = meeting.getDuration() / PER;
        List<Integer> spare = SortUtils.sortBySpare(timeList, validSequence, hit, PER);
        if (spare.isEmpty()) return Collections.emptyList();

        List<Integer> early = SortUtils.sortByDay(spare);
        if (early.isEmpty()) return Collections.emptyList();
        return SortUtils.sortByTimePriority(early);
    }
*/
    /**
     * 공통 시퀀스 계산
     */
    private static Map<Integer, Integer> getTimeSequence(Meeting meeting, int per, BiFunction<Integer, Integer, Boolean> condition) {
        //슬롯별 가능 인원 목록 만들기
        Map<Integer, List<String>> timeBlockMap = TimeUtils.flattenParticipantTimes(meeting, per);

        Map<Integer, Integer> sequenceMap = new HashMap<>();
        int total = meeting.getParticipants().size();

        // 조건(참여자 수 기준)을 만족하는 슬롯만 1로 마킹
        for (Integer time : timeBlockMap.keySet()) {
            int count = timeBlockMap.get(time).size();
            if (condition.apply(count, total)) {
                sequenceMap.put(time, 1);
            }
        }

        List<Integer> sortedTimes = new ArrayList<>(sequenceMap.keySet());
        Collections.sort(sortedTimes);


        /*
        * 인접한 두 슬롯이 정확히 per(30분) 차이면 연속으로 간주.
        * 뒤에서 앞으로 가며 누적 길이 DP를 한다.
        * 맨 끝 슬롯은 이미 1.
        * 그 앞 슬롯이 바로 다음 슬롯과 연속이면, 현재 길이 = 1 + (다음 슬롯의 길이)로 덧붙여 길어짐

        예시
        * 조건 통과 슬롯: 09:00, 09:30, 10:00 → 모두 30분 간격
        * 초기: {09:00:1, 09:30:1, 10:00:1}
        * 누적 후: {09:00:3, 09:30:2, 10:00:1}
        * 의미: 09:00부터 3슬롯(=90분) 연속 가능, 09:30부터 2슬롯(=60분)*/
        for (int i = sortedTimes.size() - 2; i >= 0; i--) {
            int cur = sortedTimes.get(i);
            int next = sortedTimes.get(i + 1);
            if (next - cur == per) {
                sequenceMap.put(cur, sequenceMap.get(cur) + sequenceMap.get(next));
            }
        }

        return sequenceMap;
    }
}
