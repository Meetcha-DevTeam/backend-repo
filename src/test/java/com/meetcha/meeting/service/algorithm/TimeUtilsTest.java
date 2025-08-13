package com.meetcha.meeting.service.algorithm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TimeUtil.flattenParticipantTimes 단위 테스트
 * <p>
 * - 슬롯 간격(per) 기준으로 end 미포함(range[start, end) ) 확인
 * - 여러 참가자/ 여러 구간 병합 확인
 * - 비어있는 입력 처리 확인
 */

class TimeUtilsTest {

    private static final int PER = 30;

    private Meeting meetingOf(List<Participant> participants) {
        // 테스트예 영향이 없는 값들은 기본 값 사용
        return new Meeting(
                "m-1",
                participants,
                0, 0, null, null, List.of(), 0, List.of()
        );

    }

    @Test
    @DisplayName("단일 참가자 - 단일 Time Range를 30분 단위로 잘 분할한다.")
    void flatten_singleParticipant_singleRange() {
        //given
        Participant a = new Participant("A", List.of(new TimeRange(600, 660))); //10:00 ~11:00를 의미
        Meeting meeting = meetingOf(List.of(a));

        //when
        Map<Integer, List<String>> map = TimeUtils.flattenParticipantTimes(meeting, PER);

        //then
        assertEquals(2, map.size()); //600, 630 두 슬롯
        assertEquals(Set.of("A"), new HashSet<>(map.get(600)));
        assertEquals(Set.of("A"), new HashSet<>(map.get(630)));
        assertFalse(map.containsKey(660)); //End 미포함 검증
    }

    @Test
    @DisplayName("두 참가자 - 겹치는 슬롯에 모든 ID가 포함된다.")
    void flatten_twoParticipant_overlap() {
        //given
        Participant a = new Participant("A", List.of(new TimeRange(600, 660)));
        Participant b = new Participant("B", List.of(new TimeRange(630, 690)));
        Meeting meeting = meetingOf(List.of(a, b));

        //when
        Map<Integer, List<String>> map = TimeUtils.flattenParticipantTimes(meeting, PER);

        //then
        assertEquals(Set.of("A"), new HashSet<>(map.get(600)));
        assertEquals(Set.of("A", "B"), new HashSet<>(map.get(630)));
        assertEquals(Set.of("B"), new HashSet<>(map.get(660)));
    }

    @Test
    @DisplayName("TimRange end가 per 경계가 아니면 start 슬롯만 생성한다. ")
    void flatten_nonAlignedEnd_includesStartOnly() {
        //given
        Participant a = new Participant("A", List.of(new TimeRange(600, 615)));
        Meeting meeting = meetingOf(List.of(a));

        //when
        Map<Integer, List<String>> map = TimeUtils.flattenParticipantTimes(meeting, PER);

        //then
        assertEquals(1, map.size());
        assertEquals(Set.of("A"), new HashSet<>(map.get(600)));
        assertFalse(map.containsKey(615));
    }

    @Test
    @DisplayName("같은 참가자가 겹치는 TimeRange를 제출해도 슬롯에는 한번만 포함된다.")
    void flatten_sameParticipant_overlappingRanges_shouldDeduplicateIds() {
        //given
        Participant a = new Participant("A", List.of(new TimeRange(600, 660), new TimeRange(630, 690)));
        Meeting meeting =meetingOf(List.of(a));
        //when
        Map<Integer,List<String>> map =TimeUtils.flattenParticipantTimes(meeting,PER);
        //then
        assertEquals(Set.of("A"),new HashSet<>(map.get(600)));
        assertEquals(Set.of("A"),new HashSet<>(map.get(630))); // 여기서 중복 있으면 안된
        assertEquals(Set.of("A"),new HashSet<>(map.get(660)));
    }

    @Test
    @DisplayName("참여자가 없는 경우 빈 맵을 반환한다.")
    void flatten_emptyParticipants_returnsEmptyMap(){
        //given
        Meeting meeting=meetingOf(List.of());

        //when
        Map<Integer, List<String>> map =TimeUtils.flattenParticipantTimes(meeting,PER);

        //then
        assertTrue(map.isEmpty());
    }


}





























