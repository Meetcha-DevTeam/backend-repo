package com.meetcha.joinmeeting.controller;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.util.AuthHeaderUtils;
import com.meetcha.joinmeeting.dto.GetSelectedTime;
import com.meetcha.joinmeeting.dto.JoinMeetingRequest;
import com.meetcha.joinmeeting.dto.JoinMeetingResponse;
import com.meetcha.joinmeeting.dto.ValidateMeetingCodeResponse;
import com.meetcha.joinmeeting.service.JoinMeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.meetcha.meeting.dto.MeetingInfoResponse;

import java.rmi.server.UID;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/meeting")
@RequiredArgsConstructor
public class JoinMeetingController {

    private final JoinMeetingService joinMeetingService;
    private JwtProvider jwtProvider;

    //미팅 참여
    @PostMapping("/id/{meetingId}/join")
    public ResponseEntity<ApiResponse<JoinMeetingResponse>> joinMeeting(
            @PathVariable UUID meetingId,
            @RequestBody JoinMeetingRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        log.debug("joinMeeting 메서드 진입");
        JoinMeetingResponse response = joinMeetingService.join(meetingId, request, authorizationHeader);
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

    @GetMapping("/{meetingId}/available-times")
    public ResponseEntity<ApiResponse<List<GetSelectedTime>>> getMyAvailableTimes(
            @PathVariable UUID meetingId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));
        ApiResponse<List<GetSelectedTime>> response =
                joinMeetingService.getMyAvailableTimes(meetingId, userId);
        return ResponseEntity.ok(response);
    }
}