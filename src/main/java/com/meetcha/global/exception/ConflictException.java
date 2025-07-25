package com.meetcha.global.exception;

import lombok.Getter;

//409 Conflict 오류감싸기
@Getter
public class ConflictException extends RuntimeException {

    private final ErrorCode errorCode;

    public ConflictException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}