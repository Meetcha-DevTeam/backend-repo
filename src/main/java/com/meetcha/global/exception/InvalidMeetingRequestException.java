package com.meetcha.global.exception;

import java.util.Map;

public class InvalidMeetingRequestException extends RuntimeException {
    private final Map<String, String> fieldErrors;
    private final ErrorCode errorCode;

    public InvalidMeetingRequestException(Map<String, String> fieldErrors) {
        super(ErrorCode.INVALID_MEETING_REQUEST.getMessage());
        this.errorCode = ErrorCode.INVALID_MEETING_REQUEST;
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
