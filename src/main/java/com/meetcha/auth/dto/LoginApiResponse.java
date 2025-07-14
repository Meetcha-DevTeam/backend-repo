package com.meetcha.auth.dto;

import lombok.Getter;

@Getter
public class LoginApiResponse<T> {

    private final boolean isSuccess;
    private final int code;
    private final String message;
    private final T data;

    private LoginApiResponse(boolean isSuccess, int code, String message, T data) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> LoginApiResponse<T> success(int code, String message, T data) {
        return new LoginApiResponse<>(true, code, message, data);
    }

    public static <T> LoginApiResponse<T> fail(int code, String message) {
        return new LoginApiResponse<>(false, code, message, null);
    }
}
