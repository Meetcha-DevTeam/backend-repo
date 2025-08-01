package com.meetcha.meeting.service.algorithm;

//POJO
// aws lambda 확장을 위해 라이브러리  최소 사용(lombok X)

/**
 * 특정 시간 범위를 표현하는 클래스.
 * 시간 단위는 분(minute) 단위로 표현된다.
 * 예: 600 = 10:00, 630 = 10:30
 */
public class TimeRange {
    private int start;
    private int end;

    // 기본 생성자 (직렬화/역직렬화용)
    public TimeRange() {}

    public TimeRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    // Getter
    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

}
/*
* 사용 예시
* TimeRange tr = new TimeRange(600, 660); // 10:00 ~ 11:00
* int duration = tr.getEnd() - tr.getStart(); // 60분
*
* */