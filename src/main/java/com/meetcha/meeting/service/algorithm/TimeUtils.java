package com.meetcha.meeting.service.algorithm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 참여자들의 가능한 시간 범위를 평탄화한다.
 *
 * 목적
 * - 시간 분해: 30분 단위로 시간대를 나눔
 * - 참여자 매핑: 각 시간마다 가능한 참여자 리스트 구성
 * - 기반 구조: MeetingTimeCalculator에서 가능한 연속 시간 계산에 사용됨
 *
 *  예시 동작
 * 입력:
 *   참가자 A: 10:00~11:00 (600~660)
 *   참가자 B: 10:30~11:30 (630~690)
 *
 * 결과:
 *   {
 *     600 → [A],
 *     630 → [A, B],
 *     660 → [B]
 *   }
 *
 * @param meeting 대상 미팅 (참여자 포함)
 * @param per 시간 단위 (예: 30분)
 * @return 시간별 참여자 ID 리스트 (Map<분 단위 시간, 참여자 ID 목록>)
 */
//시간 변환, flatten
public class TimeUtils {

    /**
     * 참여자들의 가능한 시간 범위를 평탄화한다.
     * key: 시간(분 단위), value: 해당 시간에 가능한 사용자 ID 리스트
     */
    public static Map<Integer, List<String>> flattenParticipantTimes(Meeting meeting, int per) {
        Map<Integer, List<String>> timeMap = new HashMap<>();

        for (Participant participant : meeting.getParticipants()) {
            for (TimeRange timeRange : participant.getTimeRanges()) {
                for (int time = timeRange.getStart(); time < timeRange.getEnd(); time += per) {
                    timeMap.computeIfAbsent(time, k -> new java.util.ArrayList<>())
                            .add(participant.getId());
                }
            }
        }

        return timeMap;
    }
}
