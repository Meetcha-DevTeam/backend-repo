package com.meetcha.meeting.service.algorithm;

import java.util.*;

/**
 * 가능한 시간 후보군을 우선순위에 따라 정렬하는 유틸리티 클래스
 */
//우선순위 정렬 유틸
public class SortUtils {

    /**
     * 앞뒤로 여유시간이 많은 시간대를 우선 정렬
     * @param timeList 연속 블록 길이 기준으로 최대값 후보의 "시작 시각"들(또는 전체 시작 시각들)
     * @param seqMap   key: 시작 시각, value: 해당 시각부터의 연속 블록 수
     * @param hit      회의 길이(블록 수)
     * @param per      블록 단위(분)
     *
     */
    public static List<Integer> sortBySpare(List<Integer> timeList, Map<Integer, Integer> seqMap, int hit, int per) {
        if (timeList == null || timeList.isEmpty()) return new ArrayList<>();

        // 가장 긴 연속 길이 계산(기존에는 '시간값'을 길이로 착각)
        int maxLen = 0;
        for (Integer t : timeList) {
            Integer len = seqMap.get(t);
            if (len != null) maxLen = Math.max(maxLen, len);
        }
        if (maxLen < hit) return new ArrayList<>();

        // 가운데로 당길 블록 수(mid) = (최대연속길이 - 회의길이)/2
        int midBlocks = (maxLen - hit) / 2;

        // 최대 연속 길이를 가진 구간들의 가운데 시작시각을 후보로 만든다.
        List<Integer> result = new ArrayList<>();
        for (Integer t : timeList) {
            Integer len = seqMap.get(t);
            if (len != null && len == maxLen) {
                result.add(t + midBlocks * per);
            }
        }
        // 정렬은 이후 단계에서 우선순위 정렬을 하므로 여기서는 원본 순서 유지 또는 간단 정렬만
        return result;
    }

    /**
     * 가장 빠른 날짜에 해당하는 시간들만 추출
     */
    public static List<Integer> sortByDay(List<Integer> timeList) {
        if (timeList == null || timeList.isEmpty()) return Collections.emptyList();
        final int day = 24 * 60;
        List<Integer> sorted = new ArrayList<>(timeList);
        Collections.sort(sorted, Comparator.reverseOrder());

        int refDay = sorted.get(sorted.size() - 1) / day;
        List<Integer> result = new ArrayList<>();

        for (int i = sorted.size() - 1; i >= 0 && sorted.get(i) / day == refDay; i--) {
            result.add(sorted.get(i));
        }

        return result;
    }

    /**
     * 선호 시간대(낮/오후/저녁) 우선순위 정렬
     */
    public static List<Integer> sortByTimePriority(List<Integer> timeList) {
        int[][] priorityRanges = {
                {60 * 12, 60 * 16 - 1}, // 12~16
                {60 * 16, 60 * 20 - 1}, // 16~20
                {60 * 20, 60 * 24 - 1}, // 20~24
                {60 * 8,  60 * 12 - 1}, // 8~12
                {60 * 0,  60 * 4 - 1},  // 0~4
                {60 * 4,  60 * 8 - 1}   // 4~8
        };

        Comparator<Integer> comparator = Comparator.comparingInt(time -> {
            int timeOfDay = time % (60 * 24);
            for (int i = 0; i < priorityRanges.length; i++) {
                int[] range = priorityRanges[i];
                if (timeOfDay >= range[0] && timeOfDay <= range[1]) {
                    return i;
                }
            }
            return priorityRanges.length;
        });

        List<Integer> sorted = new ArrayList<>(timeList);
        sorted.sort(comparator);
        return sorted;
    }
}

/*
사용 예시

List<Integer> filtered = SortUtils.sortBySpare(timeList, hit, 30);
filtered = SortUtils.sortByDay(filtered);
filtered = SortUtils.sortByTimePriority(filtered);
*/
