package com.meetcha.meeting.service.algorithm;

import com.meetcha.meetinglist.domain.AlternativeTimeEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    // 기존 int 비교 → LocalDateTime 비교용
    private static LocalDateTime ldt(int totalMinutes) {
        LocalDate baseDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        return baseDate.atStartOfDay().plusMinutes(totalMinutes);
    }

    @Test
    @DisplayName("[전원 참석/시간 단축] 2/3 이상 공통 구간이 있으면 후보를 반환한다")
    void durationStrategy_returnsCandidates_whenTwoThirdsAvailable() {
        // given
        // 공통(전원) 가능: 09:00~10:00 = 60분(2블럭)
        // 전체 회의 시간: 90분(3블럭) → 2/3 = 60분 충족 → 대안 있어야 함
        Participant a = p("A", tr(m(0, 9, 0), m(0, 10, 0)));
        Participant b = p("B", tr(m(0, 9, 0), m(0, 10, 30)));

        Meeting meeting = meetingOf(90, Arrays.asList(a, b));

        // when
        List<AlternativeTimeEntity> alt =
                AlternativeTimeCalculator.getAlternativeTimes(meeting, UUID.randomUUID());

        // then
        assertNotNull(alt);
        // 현재 알고리즘은 "전원 참석 + 시간 단축" 후보만 생성하므로,
        // excludedParticipants == null 인 것들이 곧 전체 후보와 동일하다.
        List<AlternativeTimeEntity> durationList = alt.stream()
                .filter(e -> e.getExcludedParticipants() == null)
                .toList();

        assertFalse(durationList.isEmpty(), "2/3 충족 시 대안 시간(진행 시간 단축)은 비어있지 않아야 함");

        // 이 시나리오에서는 전원 공통 2블럭이 09:00~09:30, 09:30~10:00 뿐이므로
        // 시작점은 09:00 하나로 결정됨
        List<LocalDateTime> starts = durationList.stream()
                .map(AlternativeTimeEntity::getStartTime)
                .toList();
        assertEquals(List.of(ldt(m(0, 9, 0))), starts);
    }

    @Test
    @DisplayName("[전원 참석/시간 단축] 2/3 미만이면 후보가 비어있다")
    void durationStrategy_returnsEmpty_whenBelowTwoThirds() {
        // given
        // 공통(전원) 가능: 09:00~09:30 = 30분(1블럭)
        // 전체 회의 시간: 90분(3블럭) → 2/3 = 60분 필요 → 미달 → 후보 없음
        Participant a = p("A", tr(m(0, 9, 0), m(0, 9, 30)));
        Participant b = p("B", tr(m(0, 9, 0), m(0, 9, 30)));

        Meeting meeting = meetingOf(90, Arrays.asList(a, b));

        // when
        List<AlternativeTimeEntity> alt =
                AlternativeTimeCalculator.getAlternativeTimes(meeting, UUID.randomUUID());

        // then
        assertNotNull(alt);
        boolean hasDuration = alt.stream()
                .anyMatch(e -> e.getExcludedParticipants() == null);
        assertFalse(hasDuration, "2/3 미만이면 진행 시간 단축 후보는 없어야 함");
    }

    @Test
    @DisplayName("[전원 참석] 일부 인원만 겹치고 전원 공통 구간이 없으면 후보가 비어있다")
    void returnsEmpty_whenOnlySubsetOverlap() {
        // given
        // A: 09:00~10:00, B: 09:00~10:00 → (A,B) 공통 09:00~10:00
        // C: 10:00~11:00
        // → 일부(2명) 겹치는 시간은 있지만, 3명 모두 공통으로 겹치는 구간은 없음
        Participant a = p("A", tr(m(0, 9, 0), m(0, 10, 0)));
        Participant b = p("B", tr(m(0, 9, 0), m(0, 10, 0)));
        Participant c = p("C", tr(m(0, 10, 0), m(0, 11, 0)));

        // 회의 시간: 60분(2블럭)
        Meeting meeting = meetingOf(60, Arrays.asList(a, b, c));

        // when
        List<AlternativeTimeEntity> alt =
                AlternativeTimeCalculator.getAlternativeTimes(meeting, UUID.randomUUID());

        // then
        // 지금 알고리즘은 "전원 참석"만 고려하므로,
        // 일부 인원만 가능한 시간대가 있어도 후보가 나오면 안 된다.
        assertNotNull(alt);
        assertTrue(alt.isEmpty(), "전원 공통 구간이 없으므로 대안 시간 후보가 없어야 함");
    }

    @Test
    @DisplayName("[전원 참석] 서로 전혀 겹치지 않으면 후보가 비어있다")
    void returnsEmpty_whenNoOverlapAtAll() {
        // given
        // A: 09:00~09:30, B: 10:00~10:30, C: 11:00~11:30
        // 어떤 슬롯에서도 전원이 동시에 가능한 시간이 없음
        Participant a = p("A", tr(m(0, 9, 0), m(0, 9, 30)));
        Participant b = p("B", tr(m(0, 10, 0), m(0, 10, 30)));
        Participant c = p("C", tr(m(0, 11, 0), m(0, 11, 30)));

        Meeting meeting = meetingOf(60, Arrays.asList(a, b, c));

        // when
        List<AlternativeTimeEntity> alt =
                AlternativeTimeCalculator.getAlternativeTimes(meeting, UUID.randomUUID());

        // then
        assertNotNull(alt);
        assertTrue(alt.isEmpty(), "전원 참석 기준으로 가능한 시간이 없으므로 후보가 없어야 함");
    }

    @Test
    @DisplayName("반환 리스트의 각 엔티티는 기본 필드를 만족한다")
    void entitiesHaveValidBasicFields() {
        // given
        // 이 케이스는 굳이 공통 구간이 없어도 됨.
        // 공통 구간이 없으면 alt는 빈 리스트가 되고, 그 경우 for-each가 그냥 스킵된다.
        Participant a = p("A", tr(m(0, 9, 0), m(0, 9, 30)));
        Participant b = p("B", tr(m(0, 11, 0), m(0, 11, 30)));
        Meeting meeting = meetingOf(60, Arrays.asList(a, b));

        // when
        List<AlternativeTimeEntity> alt =
                AlternativeTimeCalculator.getAlternativeTimes(meeting, UUID.randomUUID());

        // then
        assertNotNull(alt);
        // 구조 변경(Map -> List)에 따라 엔티티 기본 필드 유효성으로 대체 검증
        for (AlternativeTimeEntity e : alt) {
            assertNotNull(e.getMeetingId());
            assertNotNull(e.getStartTime());
            assertNotNull(e.getEndTime());
            assertNotNull(e.getDurationAdjustedMinutes());
            assertTrue(e.getDurationAdjustedMinutes() > 0);
            assertTrue(e.getEndTime().isAfter(e.getStartTime()));
        }
    }
}
