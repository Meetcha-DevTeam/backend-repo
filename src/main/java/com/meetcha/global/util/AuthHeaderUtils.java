package com.meetcha.global.util;

import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;

public class AuthHeaderUtils {

    private AuthHeaderUtils() {}

    public static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }
        String h = authorizationHeader.trim();
        if (h.toLowerCase().startsWith("bearer ")) {
            return h.substring(7).trim();
        }
        throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
    }
}
