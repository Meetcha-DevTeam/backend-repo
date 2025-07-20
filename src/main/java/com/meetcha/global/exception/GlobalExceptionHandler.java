package com.meetcha.global.exception;

import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.exception.InvalidMeetingRequestException;
import com.meetcha.global.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidGoogleCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidGoogleCode(InvalidGoogleCodeException e) {
        return ResponseEntity.status(401).body(ApiResponse.fail(401, e.getMessage(), null));
    }

    @ExceptionHandler(RefreshTokenInvalidException.class)
    public ResponseEntity<ApiResponse<Void>> handleRefreshTokenInvalid(RefreshTokenInvalidException e) {
        return ResponseEntity.status(401).body(ApiResponse.fail(401, e.getMessage(), null));
    }

    @ExceptionHandler(InvalidMeetingRequestException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleInvalidMeetingRequest(InvalidMeetingRequestException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(400, e.getMessage(), e.getFieldErrors()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(401, e.getMessage(), null));
    }

    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtExpired(io.jsonwebtoken.ExpiredJwtException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(401, "JWT 토큰이 만료되었습니다.", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(500, "알 수 없는 서버 오류가 발생했습니다.", null));
    }
}
