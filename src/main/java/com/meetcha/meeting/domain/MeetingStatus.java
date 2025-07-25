package com.meetcha.meeting.domain;

public enum MeetingStatus {
    MATCHING,       // 매칭 중
    MATCH_FAILED,   // 매칭 실패
    BEFORE,  //미팅 시작 전
    ONGOING,  //미팅 진행 중
    DONE  //미팅 완료
}
