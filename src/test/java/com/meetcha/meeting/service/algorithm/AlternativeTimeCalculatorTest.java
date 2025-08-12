package com.meetcha.meeting.service.algorithm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AlternativeTimeCalculatorTest {

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
        return new Meeting(
                "m-1",
                ps,
                0, durationMin, null, null, List.of(), 0, List.of()
        );
    }

    @Test
    @DisplayName("[전략1] 2/3 이상 공통 구간이 있으면 후보를 반환한다")
    void durationStrategy_returnsCandidates_whenTwoThirdsAvailable() {
        // given
        // 공통(전원) 가능: 09:00~10:00 = 60분(2블럭)
        // 전체 회의 시간: 90분(3블럭) → 2/3 = 60분 충족 → 대안 있어야 함
        Participant a = p("A", tr(m(0, 9, 0), m(0, 10, 0)));
        Participant b = p("B", tr(m(0, 9, 0), m(0, 10, 30)));

        Meeting meeting = meetingOf(90, Arrays.asList(a, b));

        // when
        Map<String, List<Integer>> alt = AlternativeTimeCalculator.getAlternativeTimes(meeting);

        // then
        assertNotNull(alt);
        assertTrue(alt.containsKey("duration"));
        List<Integer> durationList = alt.get("duration");
        assertNotNull(durationList);
        assertFalse(durationList.isEmpty(), "2/3 충족 시 대안 시간(진행 시간 단축)은 비어있지 않아야 함");

        // 이 시나리오에서는 전원 공통 2블럭이 09:00~09:30, 09:30~10:00 뿐이므로 시작점은 09:00 하나로 결정됨
        assertEquals(List.of(m(0, 9, 0)), durationList);
    }

    @Test
    @DisplayName("[전략1] 2/3 미만이면 후보가 비어있다")
    void durationStrategy_returnsEmpty_whenBelowTwoThirds() {
        // given
        // 공통(전원) 가능: 09:00~09:30 = 30분(1블럭)
        // 전체 회의 시간: 90분(3블럭) → 2/3 = 60분 필요 → 미달 → 후보 없음
        Participant a = p("A", tr(m(0, 9, 0), m(0, 9, 30)));
        Participant b = p("B", tr(m(0, 9, 0), m(0, 9, 30)));

        Meeting meeting = meetingOf(90, Arrays.asList(a, b));

        // when
        Map<String, List<Integer>> alt = AlternativeTimeCalculator.getAlternativeTimes(meeting);

        // then
        assertNotNull(alt);
        assertTrue(alt.containsKey("duration"));
        assertTrue(alt.get("duration").isEmpty(), "2/3 미만이면 진행 시간 단축 후보는 없어야 함");
    }

    @Test
    @DisplayName("[전략2] 일부 참여자(3명 중 2명)로 줄이면 후보를 반환한다")
    void participantStrategy_returnsCandidates_whenTwoOfThreeOverlap() {
        // given
        // A: 09:00~10:00, B: 09:00~10:00 → (A,B) 공통 09:00~10:00
        // C: 10:00~11:00 → (B,C) 10:00 슬롯은 겹치지 않음(끝 시간은 비포함이므로)
        // 전원(3명) 공통은 없음. 2명 기준으로는 09:00~10:00이 가능.
        Participant a = p("A", tr(m(0, 9, 0), m(0, 10, 0)));
        Participant b = p("B", tr(m(0, 9, 0), m(0, 10, 0)));
        Participant c = p("C", tr(m(0, 10, 0), m(0, 11, 0)));

        // 회의 시간: 60분(2블럭) → 2명 공통 09:00~10:00 내에 정확히 들어감(시작 09:00)
        Meeting meeting = meetingOf(60, Arrays.asList(a, b, c));

        // when
        Map<String, List<Integer>> alt = AlternativeTimeCalculator.getAlternativeTimes(meeting);

        // then
        assertNotNull(alt);
        assertTrue(alt.containsKey("participant"));
        List<Integer> plist = alt.get("participant");
        assertNotNull(plist);
        assertFalse(plist.isEmpty(), "2명 기준으로 가능한 시간이 있으므로 후보가 나와야 함");

        // 가능한 시작점은 09:00 하나(09:30 시작은 연속 1블럭만 남으므로 60분 불가)
        assertEquals(List.of(m(0, 9, 0)), plist);
    }

    @Test
    @DisplayName("[전략2] 서로 겹치는 인원이 없으면 후보가 비어있다")
    void participantStrategy_returnsEmpty_whenNoOverlapEvenWithReduction() {
        // given
        // A: 09:00~09:30, B: 10:00~10:30, C: 11:00~11:30
        // 어떤 30분 슬롯에서도 2명 이상이 겹치지 않음 → 2명 기준으로도 불가
        Participant a = p("A", tr(m(0, 9, 0), m(0, 9, 30)));
        Participant b = p("B", tr(m(0, 10, 0), m(0, 10, 30)));
        Participant c = p("C", tr(m(0, 11, 0), m(0, 11, 30)));

        Meeting meeting = meetingOf(60, Arrays.asList(a, b, c));

        // when
        Map<String, List<Integer>> alt = AlternativeTimeCalculator.getAlternativeTimes(meeting);

        // then
        assertNotNull(alt);
        assertTrue(alt.containsKey("participant"));
        assertTrue(alt.get("participant").isEmpty(), "2명 기준으로도 공통이 없으므로 후보가 없어야 함");
    }

    @Test
    @DisplayName("반환 맵은 duration, participant 키를 모두 포함한다")
    void resultMapAlwaysHasBothKeys() {
        // given
        Participant a = p("A", tr(m(0, 9, 0), m(0, 9, 30)));
        Participant b = p("B", tr(m(0, 11, 0), m(0, 11, 30)));
        Meeting meeting = meetingOf(60, Arrays.asList(a, b));

        // when
        Map<String, List<Integer>> alt = AlternativeTimeCalculator.getAlternativeTimes(meeting);

        // then
        assertNotNull(alt);
        assertTrue(alt.containsKey("duration"));
        assertTrue(alt.containsKey("participant"));
        assertNotNull(alt.get("duration"));
        assertNotNull(alt.get("participant"));
    }
}
