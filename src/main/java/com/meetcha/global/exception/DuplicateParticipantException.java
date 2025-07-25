package com.meetcha.global.exception;

public class DuplicateParticipantException extends Throwable {
    private final ErrorCode errorCode;

    public DuplicateParticipantException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
