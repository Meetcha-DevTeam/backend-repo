package com.meetcha.global.exception;

import lombok.Getter;

@Getter
public class InvalidGoogleCodeException extends RuntimeException {
    private final ErrorCode errorCode;

    public InvalidGoogleCodeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
