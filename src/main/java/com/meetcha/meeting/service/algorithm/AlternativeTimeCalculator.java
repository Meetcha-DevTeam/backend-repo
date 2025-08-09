package com.meetcha.meeting.service.algorithm;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 대안 시간 산출 알고리즘
 *
 * - 1차 알고리즘 실패 시 fallback 용도로 사용
 * - 두 가지 전략 제공:
 *   1. 진행 시간을 줄여 가능한 시간 확보
 *   2. 일부 참여자를 줄여 가능한 시간 확보
 */
public class AlternativeTimeCalculator {

    private static final int PER = 30;

    /**
     * 대안 시간 후보를 계산
     *
     * @param meeting 미팅 정보
     * @return Map<"duration", 후보들>, Map<"participant", 후보들>
     */
    public static Map<String, List<Integer>> getAlternativeTimes(Meeting meeting) {
        Map<String, List<Integer>> result = new HashMap<>();
        result.put("duration", getLessDurationMeetingTimes(meeting));
        result.put("participant", getLessParticipantMeetingTimes(meeting));
        return result;
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

        int maxHit = timeSequence.values().stream()
                .max(Integer::compareTo)
                .orElse(0);

        // 2/3 이상 진행 시간 확보 안되면 대안 없음
        int requiredHit = (int) Math.ceil((2.0 * meeting.getDuration() / 3) / PER);
        if (maxHit < requiredHit) return Collections.emptyList();

        List<Integer> timeList = timeSequence.keySet().stream()
                .sorted(Comparator.comparingInt(timeSequence::get))
                .collect(Collectors.toList());

        List<Integer> spare = SortUtils.sortBySpare(timeList, maxHit, PER);
        List<Integer> early = SortUtils.sortByDay(spare);
        return SortUtils.sortByTimePriority(early);
    }

    /**
     * [전략 2] 참여자를 줄여 가능한 시간 후보를 찾는다.
     */
    private static List<Integer> getLessParticipantMeetingTimes(Meeting meeting) {
        int total = meeting.getParticipants().size();
        int left = (2 * total) / 3;
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
        List<Integer> timeList = validSequence.keySet().stream()
                .sorted(Comparator.comparingInt(validSequence::get))
                .collect(Collectors.toList());

        List<Integer> spare = SortUtils.sortBySpare(timeList, meeting.getDuration() / PER, PER);
        List<Integer> early = SortUtils.sortByDay(spare);
        return SortUtils.sortByTimePriority(early);
    }

    /**
     * 공통 시퀀스 계산
     */
    private static Map<Integer, Integer> getTimeSequence(Meeting meeting, int per, BiFunction<Integer, Integer, Boolean> condition) {
        //슬롯별 가능 인원 목록 만들기
        Map<Integer, List<String>> timeMap = TimeUtils.flattenParticipantTimes(meeting, per);

        Map<Integer, Integer> sequenceMap = new HashMap<>();
        int total = meeting.getParticipants().size();

        // 조건(참여자 수 기준)을 만족하는 슬롯만 1로 마킹
        for (Integer time : timeMap.keySet()) {
            int count = timeMap.get(time).size();
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
