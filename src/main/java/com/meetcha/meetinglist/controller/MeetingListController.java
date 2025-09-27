package com.meetcha.meetinglist.controller;

import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.util.AuthHeaderUtils;
import com.meetcha.joinmeeting.dto.JoinMeetingRequest;
import com.meetcha.joinmeeting.dto.JoinMeetingResponse;
import com.meetcha.joinmeeting.service.JoinMeetingService;
import com.meetcha.meetinglist.dto.*;
import com.meetcha.meetinglist.service.AlternativeTimeService;
import com.meetcha.meetinglist.service.MeetingListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    public List<MeetingListResponse> getMyMeetingList(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));
        return meetingListService.getMyMeetings(userId);
    }

    // 미팅 상세 조회
    @GetMapping("/{meetingId}")
    public MeetingDetailResponse getMeetingDetail(
            @PathVariable UUID meetingId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return meetingListService.getMeetingDetail(meetingId, authorizationHeader);
    }



    //미팅 참가자 목록 조회
    @GetMapping("/{meetingId}/participants")
    public MeetingDetailResponse getParticipants(
            @PathVariable UUID meetingId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return meetingListService.getMeetingDetail(meetingId, authorizationHeader);
    }



    //대안 시간 후보 조회
    @GetMapping("/{meetingId}/alternative-times")
    public AlternativeTimeListResponse getAlternativeTimeList(
            @PathVariable UUID meetingId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
       return alternativeTimeService.getAlternativeTimeList(meetingId, authorizationHeader);
    }


        //대안 시간 투표 제출
    @PostMapping("/{meetingId}/alternative-vote")
    public AlternativeVoteResponse submitAlternativeVote(
            @PathVariable UUID meetingId,
            @RequestBody AlternativeVoteRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return alternativeTimeService.submitAlternativeVote(meetingId, request, authorizationHeader);
    }


        // 미팅 참여 정보 수정
    @PatchMapping("/{meetingId}")
    public JoinMeetingResponse updateParticipation(
            @PathVariable UUID meetingId,
            @RequestBody JoinMeetingRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
       return joinMeetingService.updateParticipation(meetingId, request, authorizationHeader);
    }

        //작성이 필요한 미팅 조회
    @GetMapping("/need-reflection")
    public List<NeedReflectionResponse> getFilteredMeetings(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));
        return meetingListService.getMeetingsNeedingReflection(userId);
    }
}