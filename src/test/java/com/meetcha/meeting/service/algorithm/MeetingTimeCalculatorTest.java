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
}
