package com.meetcha.global.exception;

public class ScheduleConflictException extends Throwable {
    private final ErrorCode errorCode;

    public ScheduleConflictException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
