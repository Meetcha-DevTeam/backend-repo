package com.meetcha.global.exception;

import com.meetcha.global.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    //모든 커스텀 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException e, HttpServletRequest request) {
        String path = request.getRequestURI();
        ErrorCodeBase errorCode = e.getErrorCode();

        log.error("[CustomException] path={}, errorCode={}, message={}",
                path, errorCode.getCode(), errorCode.getMessage());

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

    //@Valid 어노테이션의 에러처리를 잡기 위해서 사용
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException e, HttpServletRequest request) {
        String path = request.getRequestURI();
        Map<String, String> fieldErrors = new HashMap<>();

        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST_BODY.getHttpStatus())
                .body(ApiResponse.error(path, ErrorCode.INVALID_REQUEST_BODY, fieldErrors));
    }

    /**
     * @RequestBody의 JSON을 객체로 파싱하지 못했을 때 발생하는 예외를 처리합니다.
     * (예: 빈 {} 요청, 필드 타입 불일치 등)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        String path = request.getRequestURI();
        log.warn("Invalid JSON format request on path {}: {}", path, e.getMessage());

        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST_BODY.getHttpStatus())
                .body(ApiResponse.error(path, ErrorCode.INVALID_REQUEST_BODY));
    }
}
