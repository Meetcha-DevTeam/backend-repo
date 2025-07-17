package com.meetcha.joinmeeting.controller;

import com.meetcha.joinmeeting.dto.JoinMeetingRequest;
import com.meetcha.joinmeeting.dto.JoinMeetingResponse;
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
    public ResponseEntity<JoinMeetingResponse> joinMeeeting(
            @PathVariable UUID meetingId,
            @RequestBody JoinMeetingRequest request
    ) {
        JoinMeetingResponse response = joinMeetingService.join(meetingId, request);
        return ResponseEntity.ok(response);

    }

    //미팅 코드 유효성 검사
    @GetMapping("/code/{code}")
    public ResponseEntity<Void> validateMeetingCode(@PathVariable String code) {
        joinMeetingService.validateMeetingCode(code);
        return ResponseEntity.ok().build();
    }

    // 미팅 정보 조회
    @GetMapping("/id/{meetingId}")
    public MeetingInfoResponse getMeetingInfo(@PathVariable UUID meetingId) {
        return joinMeetingService.getMeetingInfo(meetingId);
    }

}