package com.meetcha.meeting.service.algorithm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MeetingTimeCalculatorTest {

    private static final int PER = 30;
    private static final int DAY_MIN = 24 * 60;

    private static int m(int day, int h, int min) {
        return day * DAY_MIN + h * 60 + min;
    }

    private static TimeRange tr(int start, int end) {
        return new TimeRange(start, end);
    }

    private static Participant p(String id, TimeRange... ranges) {
        return new Participant(id, Arrays.asList(ranges));
    }

    private static Meeting meetingOf(int durationMin, List<Participant> ps) {
        // 테스트예 영향이 없는 값들은 기본 값 사용
        return new Meeting(
                "m-1",
                ps,
                0, durationMin, null, null, List.of(), 0, List.of()
        );

    }

    @Test
    @DisplayName("공통 슬롯이 없으면 null을 반환한다.")
    void returnsNull_whenNoCommonBlocks() {
        // given : 서로 겹치지 않는 가용 시간 (day0 기준)
        Participant a = p("A", tr(m(0, 9, 0), m(0, 10, 0))); //09:00~ 10;00
        Participant b = p("B", tr(m(0, 11, 0), m(0, 12, 0))); //11:00~ 12;00

        Meeting meeting =meetingOf(60,Arrays.asList(a,b)); //60분 미팅

        //when
        Integer result=MeetingTimeCalculator.calculateMeetingTime(meeting);

        //then
        assertNull(result);
    }

    @Test
    @DisplayName("하나의 긴 공통 구간이 있으면 중앙 기준이되는 시간이 선택된다(여유시간이 우선적으로 선택됨). ")
    void pickMidOfLongestBlock_whenSingleMaxBlock() {
        // given : 참가자가 모두 08:00~12:00 가능 (day0 기준)
        //4시간 -> 8블럭
        Participant a = p("A", tr(m(0, 8, 0), m(0, 12, 0))); //09:00~ 10;00
        Participant b = p("B", tr(m(0, 8, 0), m(0, 12, 0))); //11:00~ 12;00

        Meeting meeting =meetingOf(60,Arrays.asList(a,b)); //60분 미팅 -> 2블럭

        //when
        Integer result=MeetingTimeCalculator.calculateMeetingTime(meeting);

        //then
        assertNotNull(result);
        assertEquals(m(0, 9, 30), result);
    }

    @Test
    @DisplayName("여러 날짜에 동일 조건의 후보가 있으면 더 이른 날짜를 선택한다.")
    void pickEarlierDay_whenSameSpareAcrossDays() {
        // given : day0과 day1 모두 14:00~18:00 (8블럭) 가능
        Participant a = p("A",
                tr(m(0, 14, 0), m(0, 18, 0)),
                tr(m(1, 14, 0), m(1, 18, 0))
        );
        Participant b = p("B",
                tr(m(0, 14, 0), m(0, 18, 0)),
                tr(m(1, 14, 0), m(1, 18, 0))
        );

        // 60분(2블럭) 미팅 → 각 날짜의 이상적 시작은 15:30
        Meeting meeting = meetingOf(60, Arrays.asList(a, b));

        // when
        Integer result = MeetingTimeCalculator.calculateMeetingTime(meeting);

        // then : 더 이른 날짜(day0) 15:30을 선택
        assertNotNull(result);
        assertEquals(m(0, 15, 30), result);
    }

    @Test
    @DisplayName("여러 겹침(동일 최장 구간이 여러 개, 여러 날짜): Spare→Day→TimePriority가 순서대로 적용된다")
    void pick_respectsAllThreePriorities_withManyOverlaps() {
        // day0: 08–12, 14–18 / day1: 08–12, 14–18  (모두 동일 길이 4시간)
        Participant a = p("A",
                tr(m(0, 8, 0), m(0, 12, 0)), tr(m(0, 14, 0), m(0, 18, 0)),
                tr(m(1, 8, 0), m(1, 12, 0)), tr(m(1, 14, 0), m(1, 18, 0))
        );
        Participant b = p("B",
                tr(m(0, 8, 0), m(0, 12, 0)), tr(m(0, 14, 0), m(0, 18, 0)),
                tr(m(1, 8, 0), m(1, 12, 0)), tr(m(1, 14, 0), m(1, 18, 0))
        );

        // 60분(2블록) 미팅 → 각 4시간 블록의 중앙 시작: 09:30, 15:30
        // 1) Spare: 4시간 블록(최장)들의 중앙들이 후보 → (day0 09:30, 15:30 / day1 09:30, 15:30)
        // 2) Day: 더 이른 날짜(day0)만 남김 → (09:30, 15:30)
        // 3) TimePriority: 12–16(최상위)인 15:30 선택
        Meeting meeting = meetingOf(60, Arrays.asList(a, b));

        Integer result = MeetingTimeCalculator.calculateMeetingTime(meeting);

        assertNotNull(result);
        assertEquals(m(0, 15, 30), result);
    }

    @Test
    @DisplayName("Spare(최장 구간)가 Day/Time보다 우선한다: 더 늦은 날짜라도 최장 구간의 중앙을 선택")
    void pick_longerBlockOnLaterDay_winsOverEarlierDayAndTimePriority() {
        // day0: 14–18 (4h) / day1: 12–22 (10h)
        Participant a = p("A", tr(m(0,14,0), m(0,18,0)), tr(m(1,12,0), m(1,22,0)));
        Participant b = p("B", tr(m(0,14,0), m(0,18,0)), tr(m(1,12,0), m(1,22,0)));

        // 60분(2블록) → day1 12–22(10h=20블록)의 중앙 시작 = 12:00 + ((20-2)/2)*30 = 16:30
        Meeting meeting = meetingOf(60, Arrays.asList(a, b));

        Integer result = MeetingTimeCalculator.calculateMeetingTime(meeting);

        assertNotNull(result);
        assertEquals(m(1, 16, 30), result); // 더 늦은 날짜라도 최장 구간이 이김
    }




}
