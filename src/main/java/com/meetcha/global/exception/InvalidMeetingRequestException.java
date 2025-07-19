package com.meetcha.global.exception;

import java.util.Map;

public class InvalidMeetingRequestException extends RuntimeException {
    private final Map<String, String> fieldErrors;

    public InvalidMeetingRequestException(Map<String, String> fieldErrors) {
        super("입력값이 유효하지 않습니다.");
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
