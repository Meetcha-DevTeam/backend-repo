package com.meetcha.meeting.service.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 가능한 시간 후보군을 우선순위에 따라 정렬하는 유틸리티 클래스
 */
//우선순위 정렬 유틸
public class SortUtils {

    /**
     * 앞뒤로 여유시간이 많은 시간대를 우선 정렬
     */
    public static List<Integer> sortBySpare(List<Integer> timeList, int hit, int per) {
        if (timeList.size() < hit) return new ArrayList<>();

        List<Integer> result = new ArrayList<>();
        int ref = timeList.get(timeList.size() - 1); // 가장 큰 값
        int mid = (ref - hit) / 2;

        for (int i = timeList.size() - 1; i >= 0 && timeList.get(i) == ref; i--) {
            result.add(timeList.get(i) + mid * per);
        }
        return result;
    }

    /**
     * 가장 빠른 날짜에 해당하는 시간들만 추출
     */
    public static List<Integer> sortByDay(List<Integer> timeList) {
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
