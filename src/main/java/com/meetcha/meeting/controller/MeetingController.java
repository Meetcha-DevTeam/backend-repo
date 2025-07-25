package com.meetcha.meeting.controller;

import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.exception.UnauthorizedException;
import com.meetcha.meeting.dto.MeetingCreateRequest;
import com.meetcha.meeting.dto.MeetingCreateResponse;
import com.meetcha.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/meeting")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final JwtProvider jwtProvider;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<MeetingCreateResponse>> createMeeting(
            @RequestBody MeetingCreateRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = extractToken(authHeader);
        UUID creatorId = jwtProvider.getUserId(token);
        MeetingCreateResponse response = meetingService.createMeeting(request, creatorId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "미팅이 성공적으로 생성되었습니다.", response));
    }


    private String extractToken(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        throw new UnauthorizedException(ErrorCode.MISSING_AUTH_TOKEN);
    }
}
