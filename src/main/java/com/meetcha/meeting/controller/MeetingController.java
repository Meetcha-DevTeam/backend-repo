package com.meetcha.meeting.controller;

import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.meeting.dto.MeetingCreateRequest;
import com.meetcha.meeting.dto.MeetingCreateResponse;
import com.meetcha.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final JwtProvider jwtProvider;

    @PostMapping
    public ResponseEntity<MeetingCreateResponse> createMeeting(
            @RequestBody MeetingCreateRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = extractToken(authHeader);
        UUID creatorBy = jwtProvider.getUserId(token);
        MeetingCreateResponse response = meetingService.createMeeting(request, creatorBy);
        return ResponseEntity.status(201).body(response);
    }

    private String extractToken(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header.");
    }
}
