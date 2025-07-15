package com.meetcha.joinmeeting.controller;

import com.meetcha.joinmeeting.dto.JoinMeetingRequest;
import com.meetcha.joinmeeting.dto.JoinMeetingResponse;
import com.meetcha.joinmeeting.service.JoinMeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/meeting")
@RequiredArgsConstructor
public class JoinMeetingController {
    //meetings/{id}/join 엔드포인트 처리
    //미팅 참가 요청을 받고 응답 반환

    private final JoinMeetingService joinMeetingService;

    @PostMapping("/{meetingId}/join")
    public ResponseEntity<JoinMeetingResponse> joinMeeeting(
            @PathVariable UUID meetingId,
            @RequestBody JoinMeetingRequest request
    ) {
        JoinMeetingResponse response = joinMeetingService.join(meetingId, request);
        return ResponseEntity.ok(response);

    }


}