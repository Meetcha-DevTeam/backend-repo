package com.meetcha.meetinglist.controller;

import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.joinmeeting.dto.JoinMeetingRequest;
import com.meetcha.joinmeeting.dto.JoinMeetingResponse;
import com.meetcha.joinmeeting.service.JoinMeetingService;
import com.meetcha.meetinglist.dto.*;
import com.meetcha.meetinglist.service.AlternativeTimeService;
import com.meetcha.meetinglist.service.MeetingListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/meeting-lists")
@RequiredArgsConstructor
public class MeetingListController {

    private final MeetingListService meetingListService;
    private final AlternativeTimeService alternativeTimeService;
    private final JoinMeetingService joinMeetingService;
    private final JwtProvider jwtProvider;

    // 미팅 목록 조회
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<MeetingListResponse>>> getMyMeetingList(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        UUID userId = getCurrentUserId();

        List<MeetingListResponse> meetings = meetingListService.getMyMeetings(userId);
        return ResponseEntity.ok(ApiResponse.success(200, "유저 미팅 목록 조회 성공", meetings));
    }

    // 미팅 상세 조회
    @GetMapping("/{meetingId}")
    public ResponseEntity<ApiResponse<MeetingDetailResponse>> getMeetingDetail(
            @PathVariable UUID meetingId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        MeetingDetailResponse response = meetingListService.getMeetingDetail(meetingId, authorizationHeader);
        return ResponseEntity.ok(ApiResponse.success(200, "미팅 상세 조회 성공", response));
    }



    //미팅 참가자 목록 조회
    @GetMapping("/{meetingId}/participants")
    public ResponseEntity<ApiResponse<MeetingDetailResponse>> getParticipants(
            @PathVariable UUID meetingId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        MeetingDetailResponse response = meetingListService.getMeetingDetail(meetingId, authorizationHeader);
        return ResponseEntity.ok(ApiResponse.success(200, "참가자 목록 조회 성공", response));
    }



    //대안 시간 후보 조회
    @GetMapping("/{meetingId}/alternative-times")
    public ResponseEntity<ApiResponse<AlternativeTimeListResponse>> getAlternativeTimeList(
            @PathVariable UUID meetingId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        AlternativeTimeListResponse response = alternativeTimeService.getAlternativeTimeList(meetingId, authorizationHeader);
        return ResponseEntity.ok(ApiResponse.success(200, "대안 시간 후보 조회 성공", response));
    }


    //대안 시간 투표 제출
    @PostMapping("/{meetingId}/alternative-vote")
    public ResponseEntity<ApiResponse<AlternativeVoteResponse>> submitAlternativeVote(
            @PathVariable UUID meetingId,
            @RequestBody AlternativeVoteRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        AlternativeVoteResponse response = alternativeTimeService.submitAlternativeVote(meetingId, request, authorizationHeader);
        return ResponseEntity.ok(ApiResponse.success(200, "대안 시간 투표 제출 성공", response));
    }


    // 미팅 참여 정보 수정
    @PatchMapping("/{meetingId}")
    public ResponseEntity<ApiResponse<JoinMeetingResponse>> updateParticipation(
            @PathVariable UUID meetingId,
            @RequestBody JoinMeetingRequest request
    ) {
        JoinMeetingResponse response = joinMeetingService.updateParticipation(meetingId, request);
        return ResponseEntity.ok(ApiResponse.success(200, "미팅 참여 정보 수정 성공", response));
    }

    //작성이 필요한 미팅 조회
    @GetMapping("/need-reflection")
    public ResponseEntity<ApiResponse<List<NeedReflectionResponse>>> getFilteredMeetings(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = authorizationHeader.replace("Bearer ", "");
        UUID userId = jwtProvider.getUserId(token);

        List<NeedReflectionResponse> meetings = meetingListService.getMeetingsNeedingReflection(userId);
        return ResponseEntity.ok(ApiResponse.success(200, "미팅 목록 조회 성공", meetings));
    }

    public UUID getCurrentUserId() {
        return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}