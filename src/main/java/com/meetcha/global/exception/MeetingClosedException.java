package com.meetcha.global.exception;

public class MeetingClosedException extends Throwable {
    private final ErrorCode errorCode;

    public MeetingClosedException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
