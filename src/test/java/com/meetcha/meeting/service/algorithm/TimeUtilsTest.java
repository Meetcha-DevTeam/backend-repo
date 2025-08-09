package com.meetcha.meeting.service.algorithm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
        assertEquals(2,map.size()); //600, 630 두 슬롯
        assertEquals(Set.of("A"), new HashSet<>(map.get(600)));
        assertEquals(Set.of("A"), new HashSet<>(map.get(630)));
        assertFalse(map.containsKey(660)); //End 미포함 검증
    }

}
