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
        List<Integer> timeList = Arrays.asList(1, 2, 3);
        int hit = 4;// 리스트 크기보다 큼
        int per = 30;

        //when
        List<Integer> result = SortUtils.sortBySpare(timeList, hit, per);

        //then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("sortBySpare: 최대값 구간의 중앙값을 반환한다.")
    void sortBySpare_returnMidpointsForMaxBlock() {
        //given
        List<Integer> timeList = Arrays.asList(1, 3, 5, 5);
        int hit = 2;
        int per = 30;
        /**
         * timeList : 각 숫자는 어떤 "시간 슬롯"의 연속 가능 시간 길이를 의미한다 → 즉, 5칸짜리 최대 연속 구간이 2개 있다
         * hit = 2 : 미팅을 진행하려면 최소 2칸(슬롯)이 필요함.
         * per = 30 : 한 칸(slot)은 30분을 의미.
         */

        //when
        List<Integer> result = SortUtils.sortBySpare(timeList, hit, per);

        //then
        assertEquals(Arrays.asList(35, 35), result);
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
