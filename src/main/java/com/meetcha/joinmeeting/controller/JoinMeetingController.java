package com.meetcha.joinmeeting.controller;

import com.meetcha.global.dto.ApiResponse;
import com.meetcha.joinmeeting.dto.JoinMeetingRequest;
import com.meetcha.joinmeeting.dto.JoinMeetingResponse;
import com.meetcha.joinmeeting.dto.ValidateMeetingCodeResponse;
import com.meetcha.joinmeeting.service.JoinMeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.meetcha.meeting.dto.MeetingInfoResponse;

import java.util.UUID;

@RestController
@RequestMapping("/meeting")
@RequiredArgsConstructor
public class JoinMeetingController {

    private final JoinMeetingService joinMeetingService;

    //미팅 참여
    @PostMapping("/id/{meetingId}/join")
    public ResponseEntity<ApiResponse<JoinMeetingResponse>> joinMeeeting(
            @PathVariable UUID meetingId,
            @RequestBody JoinMeetingRequest request
    ) {
        JoinMeetingResponse response = joinMeetingService.join(meetingId, request);
        return ResponseEntity.ok(ApiResponse.success(200, "미팅 참여 성공", response));

    }

    //미팅 코드 유효성 검사
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<ValidateMeetingCodeResponse>> validateMeetingCode(
            @PathVariable String code
    ) {
        return ResponseEntity.ok(joinMeetingService.validateMeetingCode(code));
    }


    // 미팅 정보 조회
    @GetMapping("/id/{meetingId}")
    public ResponseEntity<ApiResponse<MeetingInfoResponse>> getMeetingInfo(@PathVariable UUID meetingId) {
        MeetingInfoResponse response = joinMeetingService.getMeetingInfo(meetingId);
        return ResponseEntity.ok(ApiResponse.success(200, "미팅 정보 조회 성공", response));
    }

}