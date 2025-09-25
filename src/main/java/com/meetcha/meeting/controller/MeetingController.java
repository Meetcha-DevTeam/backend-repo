package com.meetcha.meeting.controller;

import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.annotation.AuthUser;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.util.AuthHeaderUtils;
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

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<MeetingCreateResponse>> createMeeting(
            @RequestBody MeetingCreateRequest request,
            @AuthUser UUID userId
    ) {
        MeetingCreateResponse response = meetingService.createMeeting(request, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "미팅이 성공적으로 생성되었습니다.", response));
    }
}
