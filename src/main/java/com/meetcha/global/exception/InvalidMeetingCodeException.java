package com.meetcha.global.exception;

public class InvalidMeetingCodeException extends Throwable {
    private final ErrorCode errorCode;

    public InvalidMeetingCodeException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
