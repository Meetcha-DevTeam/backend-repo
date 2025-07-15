package com.meetcha.auth.dto;

import lombok.Getter;

@Getter
public class AuthApiResponse<T> {

    private final boolean isSuccess;
    private final int code;
    private final String message;
    private final T data;

    private AuthApiResponse(boolean isSuccess, int code, String message, T data) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> AuthApiResponse<T> success(int code, String message, T data) {
        return new AuthApiResponse<>(true, code, message, data);
    }

    public static <T> AuthApiResponse<T> fail(int code, String message) {
        return new AuthApiResponse<>(false, code, message, null);
    }
}
