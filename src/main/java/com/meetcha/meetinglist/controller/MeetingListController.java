package com.meetcha.meetinglist.controller;

import com.meetcha.meeting.dto.MeetingInfoResponse;
import com.meetcha.meetinglist.dto.*;
import com.meetcha.meetinglist.service.AlternativeTimeService;
import com.meetcha.meetinglist.service.MeetingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/meeting-lists")
@RequiredArgsConstructor
public class MeetingListController {

    private final MeetingListService meetingListService;
    private final AlternativeTimeService alternativeTimeService;

    // 미팅 상세 조회
    @GetMapping("/{meetingId}")
    public ResponseEntity<MeetingDetailResponse> getMeetingDetail(
            @PathVariable UUID meetingId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return ResponseEntity.ok(meetingListService.getMeetingDetail(meetingId, authorizationHeader));
    }



    //미팅 참가자 목록 조회
    @GetMapping("/{meetingId}/participants")
    public ResponseEntity<MeetingDetailResponse> getParticipants(
            @PathVariable UUID meetingId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return ResponseEntity.ok(meetingListService.getMeetingDetail(meetingId, authorizationHeader));
    }



    //대안 시간 후보 조회
    @GetMapping("{meetingId}/alternative-times")
    public ResponseEntity<AlternativeTimeListResponse> getAlternativeTimeList(
            @PathVariable UUID meetingId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return ResponseEntity.ok(alternativeTimeService.getAlternativeTimeList(meetingId, authorizationHeader));
    }


    //대안 시간 투표 제출
    @PostMapping("/{meetingId}/alternative-vote")
    public ResponseEntity<AlternativeVoteResponse> submitAlternativeVote(
            @PathVariable UUID meetingId,
            @RequestBody AlternativeVoteRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return ResponseEntity.ok(alternativeTimeService.submitAlternativeVote(meetingId, request, authorizationHeader));
    }

}