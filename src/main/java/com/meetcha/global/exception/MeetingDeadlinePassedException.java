package com.meetcha.global.exception;

public class MeetingDeadlinePassedException extends Throwable {
    private final ErrorCode errorCode;

    public MeetingDeadlinePassedException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
