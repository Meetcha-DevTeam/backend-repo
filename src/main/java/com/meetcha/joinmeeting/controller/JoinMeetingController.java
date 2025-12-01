package com.meetcha.joinmeeting.controller;

import com.meetcha.global.annotation.AuthUser;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.joinmeeting.dto.GetSelectedTime;
import com.meetcha.joinmeeting.dto.JoinMeetingRequest;
import com.meetcha.joinmeeting.dto.JoinMeetingResponse;
import com.meetcha.joinmeeting.dto.ValidateMeetingCodeResponse;
import com.meetcha.joinmeeting.service.JoinMeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.meetcha.meeting.dto.MeetingInfoResponse;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/meeting")
@RequiredArgsConstructor
public class JoinMeetingController {

    private final JoinMeetingService joinMeetingService;

    //미팅 참여
    @PostMapping("/id/{meetingId}/join")
    public JoinMeetingResponse joinMeeting(
            @PathVariable UUID meetingId,
            @RequestBody @Valid JoinMeetingRequest request,
            @AuthUser UUID userId
    ) {
        log.debug("joinMeeting 메서드 진입");
        return joinMeetingService.join(meetingId, request, userId);
    }

    // 미팅 코드 유효성 검사
    @GetMapping("/code/{code}")
    public ValidateMeetingCodeResponse validateMeetingCode(
            @PathVariable String code
    ) {
        return joinMeetingService.validateMeetingCode(code);
    }

    // 미팅 정보 조회
    @GetMapping("/id/{meetingId}")
    public MeetingInfoResponse getMeetingInfo(@PathVariable UUID meetingId) {
        return joinMeetingService.getMeetingInfo(meetingId);
    }

    @GetMapping("/{meetingId}/available-times")
    public List<GetSelectedTime> getMyAvailableTimes(
            @PathVariable UUID meetingId,
            @AuthUser UUID userId
    ) {
         return joinMeetingService.getMyAvailableTimes(meetingId, userId);
    }
}