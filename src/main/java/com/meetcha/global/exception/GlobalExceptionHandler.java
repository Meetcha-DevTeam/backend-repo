package com.meetcha.global.exception;

import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidGoogleCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidGoogleCode(InvalidGoogleCodeException e) {
        return ResponseEntity
                .status(ErrorCode.INVALID_GOOGLE_CODE.getHttpStatus())
                .body(ApiResponse.fail(ErrorCode.INVALID_GOOGLE_CODE.getCode(), ErrorCode.INVALID_GOOGLE_CODE.getMessage(), null));
    }

    @ExceptionHandler(RefreshTokenInvalidException.class)
    public ResponseEntity<ApiResponse<Void>> handleRefreshTokenInvalid(RefreshTokenInvalidException e) {
        return ResponseEntity
                .status(ErrorCode.INVALID_REFRESH_TOKEN.getHttpStatus())
                .body(ApiResponse.fail(ErrorCode.INVALID_REFRESH_TOKEN.getCode(), ErrorCode.INVALID_REFRESH_TOKEN.getMessage(), null));
    }

    @ExceptionHandler(InvalidMeetingRequestException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleInvalidMeetingRequest(InvalidMeetingRequestException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage(), e.getFieldErrors()));
    }



    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity
                .status(ErrorCode.MISSING_AUTH_TOKEN.getHttpStatus())
                .body(ApiResponse.fail(ErrorCode.MISSING_AUTH_TOKEN.getCode(), ErrorCode.MISSING_AUTH_TOKEN.getMessage(), null));
    }

    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtExpired(io.jsonwebtoken.ExpiredJwtException e) {
        return ResponseEntity
                .status(ErrorCode.EXPIRED_JWT.getHttpStatus())
                .body(ApiResponse.fail(ErrorCode.EXPIRED_JWT.getCode(), ErrorCode.EXPIRED_JWT.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception e) {
        log.error("Unhandled exception occurred", e);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage(), null));
    }

    @ExceptionHandler(InvalidJoinMeetingRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidJoinMeetingRequest(InvalidJoinMeetingRequestException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage(), null));
    }

}
