package com.meetcha.global.exception;

import lombok.Getter;

@Getter
public class RefreshTokenInvalidException extends RuntimeException {
    private final ErrorCode errorCode;

    public RefreshTokenInvalidException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
