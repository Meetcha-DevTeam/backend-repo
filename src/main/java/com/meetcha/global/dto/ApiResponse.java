package com.meetcha.global.dto;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final boolean isSuccess;
    private final int code;
    private final String message;
    private final T data;

    private ApiResponse(boolean isSuccess, int code, String message, T data) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return new ApiResponse<>(true, code, message, data);
    }

    public static <T> ApiResponse<T> fail(int code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }

    // 반환데이터가 없는 경우
    public static <T> ApiResponse<T> success(int code, String message) {
        return new ApiResponse<>(true, code, message, null);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

}
