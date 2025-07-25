package com.meetcha.global.exception;

public class AlreadyJoinedMeetingException extends Throwable {
    private final ErrorCode errorCode;

    public AlreadyJoinedMeetingException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
