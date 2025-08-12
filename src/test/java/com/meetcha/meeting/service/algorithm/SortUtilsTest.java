package com.meetcha.meeting.service.algorithm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SortUtilsTest {

    @Test
    @DisplayName("sortBySpare: 후보가 hit 개수보다 적으면 빈 리스트 반환")
    void sortBySpare_returnsEmpty_whenListSmallerThanHit() {
        //Given
        int hit = 4;// 리스트 크기보다 큼
        int per = 30;

        // timeSequence: 시작시간 -> 그 시점부터의 연속 슬롯 수
        Map<Integer, Integer> timeSequence = new HashMap<>();
        timeSequence.put(0, 1);
        timeSequence.put(30, 2);
        // 최대 연속 길이 = 2 < hit(4)

        // timeList는 timeSequence 값을 기준으로 오름차 정렬된 "시작시간" 목록
        List<Integer> timeList = new ArrayList<>(timeSequence.keySet());
        timeList.sort(Comparator.comparingInt(timeSequence::get));

        //when
        List<Integer> result = SortUtils.sortBySpare(timeList, timeSequence, hit, per);

        //then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("sortBySpare: 최대값 구간의 중앙값을 반환한다.")
    void sortBySpare_returnMidpointsForMaxBlock() {
        //given
        int hit = 2;
        int per = 30;
        // 두 개의 최대(=5칸) 연속 구간이 존재하는 케이스 구성
        // 0에서 시작해 5칸(150분), 240에서 시작해 5칸(150분)
        // 중앙 시작시간 = start + ((5 - 2) / 2)*per = start + 30
        Map<Integer, Integer> timeSequence = new HashMap<>();
        timeSequence.put(120, 1);
        timeSequence.put(60, 3);
        timeSequence.put(0, 5);
        timeSequence.put(240, 5);

        //  값(연속 길이) 오름차 → 길이가 같으면 시간 오름차 정렬
        List<Integer> timeList = new ArrayList<>(timeSequence.keySet());
        timeList.sort(
                Comparator.comparingInt(timeSequence::get)
                        .thenComparingInt(t -> (int) t)
        );

        //when
        List<Integer> result = SortUtils.sortBySpare(timeList, timeSequence, hit, per);

        //then
        assertEquals(Arrays.asList(30, 270), result);
    }

    @Test
    @DisplayName("sortByDay: 가장 이른 날짜의 시간만 반환한다.")
    void sortByDay_keepsOnlyEarlistDay() {
        //given
        final int day = 24 * 60;
        List<Integer> timeList = Arrays.asList(1600, 200, 100);

        //when
        List<Integer> result = SortUtils.sortByDay(timeList);

        //then
        assertEquals(Arrays.asList(100, 200), result);
    }

    @Test
    @DisplayName("sortByTimePriority: 우선순위 규칙대로 정렬한다.")
    void sortByTimePriority_orderByConfigureWindows(){
        //given
        final int M=60;
        List<Integer> times =Arrays.asList(
                9 * M,                 // 8~12
                12 * M,                // 12~16
                16 * M,                // 16~20
                21 * M,                // 20~24
                2 * M,                 // 0~4
                5 * M,                 // 4~8
                15 * M,                // 12~16
                24 * M + 13 * M        // 다음날 12~16
        );

        // When
        List<Integer> result = SortUtils.sortByTimePriority(times);

        // Then
        List<Integer> expected = Arrays.asList(
                12 * M, 15 * M, 24 * M + 13 * M,
                16 * M,
                21 * M,
                9 * M,
                2 * M,
                5 * M
        );
        assertEquals(expected, result);
    }

}
