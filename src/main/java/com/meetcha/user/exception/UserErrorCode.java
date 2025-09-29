package com.meetcha.user.exception;

import com.meetcha.global.exception.ErrorCodeBase;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCode implements ErrorCodeBase {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "미팅을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    UserErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public int getCode() {
        return httpStatus.value();
    }
}