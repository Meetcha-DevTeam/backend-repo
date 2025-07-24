package com.meetcha.global.exception;

import lombok.Getter;

/// todo  다른 customException 이 클래스로 통합예정
@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
