package com.meetcha.reflection.controller;

import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.annotation.AuthUser;
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

    @GetMapping("/summary")
    public GetReflectionSummaryResponse getReflectionSummary(@AuthUser UUID userId) {
        return reflectionService.getReflectionSummary(userId);
    }

}
