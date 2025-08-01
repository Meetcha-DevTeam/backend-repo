package com.meetcha.meeting.service.algorithm;

import java.util.ArrayList;
import java.util.List;
//POJO
/**
 * 미팅에 참여하는 유저를 나타내는 클래스.
 * 하나의 유저는 여러 개의 가능한 시간(TimeRange)을 가질 수 있다.
 */
public class Participant {
    private String id; // UUID.toString()으로 표현된 사용자 ID
    private List<TimeRange> timeRanges;

    // 기본 생성자 (직렬화/역직렬화용)
    public Participant() {
        this.timeRanges = new ArrayList<>();
    }

    public Participant(String id, List<TimeRange> timeRanges) {
        this.id = id;
        this.timeRanges = timeRanges;
    }

    // Getter
    public String getId() {
        return id;
    }

    public List<TimeRange> getTimeRanges() {
        return timeRanges;
    }

}


/*
사용 예시
List<TimeRange> ranges = List.of(new TimeRange(600, 660), new TimeRange(720, 750));
Participant p = new Participant("user-123", ranges);
*/
