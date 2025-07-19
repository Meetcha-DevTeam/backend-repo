package com.meetcha.global.exception;

import com.meetcha.global.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidMeetingRequestException.class)
    public ApiResponse<Map<String, String>> handleInvalidMeetingRequest(InvalidMeetingRequestException e) {
        return ApiResponse.fail(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                e.getFieldErrors()
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ApiResponse<Void> handleUnauthorized(UnauthorizedException e) {
        return ApiResponse.fail(
                HttpStatus.UNAUTHORIZED.value(),
                e.getMessage(),
                null
        );
    }

}
