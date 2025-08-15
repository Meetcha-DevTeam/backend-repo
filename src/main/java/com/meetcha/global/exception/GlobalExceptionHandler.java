package com.meetcha.global.exception;

import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 유효하지 않은 구글 인증 코드 예외 처리
    @ExceptionHandler(InvalidGoogleCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidGoogleCode(InvalidGoogleCodeException e) {
        return error(ErrorCode.INVALID_GOOGLE_CODE);
    }


    // 리프레시 토큰이 유효하지 않을 경우 예외 처리
    @ExceptionHandler(RefreshTokenInvalidException.class)
    public ResponseEntity<ApiResponse<Void>> handleRefreshTokenInvalid(RefreshTokenInvalidException e) {
        return error(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    // 리소스없을때
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
        return error(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    // 약속 생성 요청이 잘못된 경우의 예외 처리
    @ExceptionHandler(InvalidMeetingRequestException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleInvalidMeetingRequest(InvalidMeetingRequestException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage(), e.getFieldErrors()));
    }


    // 인증 정보가 없거나 유효하지 않은 경우의 예외 처리
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException e) {
        return error(ErrorCode.MISSING_AUTH_TOKEN);
    }

    // JWT 토큰이 만료된 경우 예외 처리
    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtExpired(io.jsonwebtoken.ExpiredJwtException e) {
        return error(ErrorCode.EXPIRED_JWT);
    }

    // 처리되지 않은 일반적인 예외를 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception e) {
        log.error("Unhandled exception occurred", e);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage(), null));
    }

    //  요청 바디 에러: 미팅 참가
    @ExceptionHandler(InvalidJoinMeetingRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidJoinMeetingRequest(InvalidJoinMeetingRequestException e) {
        return error(e.getErrorCode());
    }

    // JWT 토큰 형식이 잘못된 경우 예외 처리///todo 해당 예외 발생안함
    @ExceptionHandler(io.jsonwebtoken.MalformedJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformedJwt(io.jsonwebtoken.MalformedJwtException e) {
        return error(ErrorCode.MALFORMED_JWT);
    }

    // 대안시간이 유효하지 않은 경우
    @ExceptionHandler(InvalidAlternativeTimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidAlternativeTime(InvalidAlternativeTimeException e) {
        return error(e.getErrorCode());
    }

    // 미팅 참여 마감
    @ExceptionHandler(MeetingDeadlinePassedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMeetingDeadlinePassed(MeetingDeadlinePassedException e) {
        return error(ErrorCode.MEETING_DEADLINE_PASSED);
    }


    // 커스텀 예외
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return error(errorCode);
    }

    // 공통 응답 생성 메서드
    private ResponseEntity<ApiResponse<Void>> error(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage(), null));
    }

}
