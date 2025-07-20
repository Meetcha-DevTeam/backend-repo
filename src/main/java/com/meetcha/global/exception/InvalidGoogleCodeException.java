package com.meetcha.global.exception;

public class InvalidGoogleCodeException extends RuntimeException {
    public InvalidGoogleCodeException(String message) {
        super(message);
    }
}