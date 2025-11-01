package com.meetcha.global.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.meetcha.global.exception.ErrorCodeBase;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ApiResponse<T> {
    //봉투 필드 추가
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;
    private final String path;

    private final int code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String errorCode;

    private ApiResponse(String path, int code, String message, T data, String errorCode) {
        this.timestamp = LocalDateTime.now();
        this.path = path;
        this.code = code;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
    }

    //성공
    public static <T> ApiResponse<T> success(String path, int code, String message, T data) {
        return new ApiResponse<>(path, code, message, data, null);
    }

    public static <T> ApiResponse<T> success(String path, int code, String message) {
        return new ApiResponse<>(path, code, message, null, null);
    }

    public static <T> ApiResponse<T> error(String path,ErrorCodeBase errorCode) {
        return new ApiResponse<>(path, errorCode.getCode(), errorCode.getMessage(), null, errorCode instanceof Enum<?> ? ((Enum<?>) errorCode).name() : null
        );
    }

    public static <T> ApiResponse<T> error(String path,ErrorCodeBase errorCode, T data) {
        return new ApiResponse<>(path, errorCode.getCode(), errorCode.getMessage(), data, errorCode instanceof Enum<?> ? ((Enum<?>) errorCode).name() : null
        );
    }
}
