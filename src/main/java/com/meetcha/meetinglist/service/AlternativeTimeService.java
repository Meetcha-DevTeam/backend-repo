package com.meetcha.meetinglist.service;

import com.meetcha.global.dto.ApiResponse;
import com.meetcha.meetinglist.dto.AlternativeTimeListResponse;
import com.meetcha.meetinglist.dto.AlternativeVoteRequest;
import com.meetcha.meetinglist.dto.AlternativeVoteResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AlternativeTimeService {

    public ApiResponse<AlternativeTimeListResponse> getAlternativeTimeList(UUID meetingId, String authorizationHeader) {
        //대안시간 후보 조회 로직
        return null;
    }

    public ApiResponse<AlternativeVoteResponse> submitAlternativeVote(UUID meetingId, AlternativeVoteRequest request, String authorizationHeader) {
        //대안 시간 투표 제출 로직
        return null;
    }
}
