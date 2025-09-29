package com.meetcha.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCodeBase {
    HttpStatus getHttpStatus();
    String getMessage();
    int getCode();
}