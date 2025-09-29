package com.meetcha.global.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCodeBase errorCode;
    private final Map<String, String> fieldErrors;

    public CustomException(ErrorCodeBase errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.fieldErrors = null;
    }

    public CustomException(ErrorCodeBase errorCode, Map<String, String> fieldErrors) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors;
    }

}
