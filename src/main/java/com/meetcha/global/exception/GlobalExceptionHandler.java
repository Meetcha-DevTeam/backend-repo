package com.meetcha.global.exception;

import com.meetcha.global.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    //모든 커스텀 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();
        String path = request.getRequestURI();

        // fieldErrors가 있으면 data에 같이
        if (e.getFieldErrors() != null) {
            return ResponseEntity
                    .status(errorCode.getHttpStatus())
                    .body(ApiResponse.error(path,errorCode, e.getFieldErrors()));
        }

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(path, errorCode));
    }

    //처리되지 않은 예외 처리 (로그 찍고 내부 서버 오류로 통일)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception e, HttpServletRequest request) {
        log.error("알 수 없는 예외 발생", e);
        String path = request.getRequestURI();

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.error(path, ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
