package com.meetcha.global.exception;

import lombok.Getter;

@Getter
public class InvalidJoinMeetingRequestException extends RuntimeException {
    private final ErrorCode errorCode;

    public InvalidJoinMeetingRequestException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
