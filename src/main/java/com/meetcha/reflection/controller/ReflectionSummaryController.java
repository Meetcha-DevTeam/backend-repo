package com.meetcha.reflection.controller;

import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.reflection.service.MeetingReflectionService;
import com.meetcha.reflection.dto.GetReflectionSummaryResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reflection")
public class ReflectionSummaryController {

    private final MeetingReflectionService reflectionService;
    private final JwtProvider jwtProvider;

    @GetMapping("/summary")
    public GetReflectionSummaryResponse getReflectionSummary(HttpServletRequest request) {
        UUID userId = extractUserIdFromToken(request);
        return reflectionService.getReflectionSummary(userId);
    }

    private UUID extractUserIdFromToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            String token = bearer.substring(7);
            return jwtProvider.getUserId(token);
        }
        throw new CustomException(ErrorCode.MISSING_AUTH_TOKEN);
    }
}
