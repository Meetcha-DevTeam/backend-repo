package com.meetcha.meeting.service.algorithm;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 미팅 시간 산출 알고리즘
 * 참여자들의 가용 시간을 기반으로 최적의 시작 시간을 계산한다.
 */
public class MeetingTimeCalculator {

    private static final int PER = 30; // 시간 단위: 30분

    public static Integer calculateMeetingTime(Meeting meeting) {
        int hit = meeting.getDuration() / PER;

        // 1. 가능한 시간 시퀀스 추출
        Map<Integer, Integer> timeSequence = getTimeSequence(
                meeting,
                PER,
                (currentCount, totalCount) -> currentCount == totalCount
        );

        if (timeSequence.isEmpty()) return null;

        // 2. 우선순위 1: 여유 시간이 많은 시간대
        List<Integer> timeList = timeSequence.keySet().stream()
                .sorted(Comparator.comparingInt(timeSequence::get))
                .collect(Collectors.toList());

        List<Integer> spareCandidates = SortUtils.sortBySpare(timeList,timeSequence, hit, PER);
        if (spareCandidates == null || spareCandidates.isEmpty()) return null;

        // 3. 우선순위 2: 가장 빠른 날짜
        List<Integer> earlyCandidates = SortUtils.sortByDay(spareCandidates);

        // 4. 우선순위 3: 선호 시간대 우선순위
        List<Integer> finalCandidates = SortUtils.sortByTimePriority(earlyCandidates);

        return finalCandidates.isEmpty() ? null : finalCandidates.get(0);
    }

    /**
     * 가능한 시간 시퀀스를 추출한다.
     * key: 시작 시간 (분 단위), value: 연속 가능 블럭 수
     */
    private static Map<Integer, Integer> getTimeSequence(
            Meeting meeting,
            int per,
            BiFunction<Integer, Integer, Boolean> condition
    ) {
        Map<Integer, List<String>> timeBlockMap = TimeUtils.flattenParticipantTimes(meeting, per);

        Map<Integer, Integer> sequenceMap = new HashMap<>();
        int total = meeting.getParticipants().size();

        for (Integer time : timeBlockMap.keySet()) {
            int count = timeBlockMap.get(time).size();
            if (condition.apply(count, total)) {
                sequenceMap.put(time, 1);
            }
        }

        List<Integer> sortedTimes = new ArrayList<>(sequenceMap.keySet());
        Collections.sort(sortedTimes);

        for (int i = sortedTimes.size() - 2; i >= 0; i--) {
            int current = sortedTimes.get(i);
            int next = sortedTimes.get(i + 1);
            if (next - current == per) {
                sequenceMap.put(current, sequenceMap.get(current) + sequenceMap.get(next));
            }
        }

        return sequenceMap;
    }
}
