package com.meetcha.global.exception;

import java.util.Map;

public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, String> fieldErrors;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.fieldErrors = null;
    }

    public CustomException(ErrorCode errorCode, Map<String, String> fieldErrors) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
