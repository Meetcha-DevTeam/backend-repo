package com.meetcha.global.exception;

import lombok.Getter;

@Getter
public class InvalidAlternativeTimeException extends RuntimeException {
    private final ErrorCode errorCode;

    public InvalidAlternativeTimeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
