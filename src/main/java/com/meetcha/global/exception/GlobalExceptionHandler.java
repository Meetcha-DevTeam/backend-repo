package com.meetcha.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidMeetingRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidMeetingRequest(InvalidMeetingRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "code", 400,
                "message", e.getMessage(),
                "errors", e.getFieldErrors()
        ));
    }
}
