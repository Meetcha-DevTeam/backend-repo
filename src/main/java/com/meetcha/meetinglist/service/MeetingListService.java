package com.meetcha.meetinglist.service;

import com.meetcha.global.dto.ApiResponse;
import com.meetcha.meetinglist.dto.MeetingDetailResponse;
import com.meetcha.meetinglist.dto.ParticipantsResponse;
import com.meetcha.meetinglist.dto.ParticipationUpdateRequest;
import com.meetcha.meetinglist.dto.ParticipationUpdateResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MeetingListService {
    public ApiResponse<MeetingDetailResponse> getMeetingDetail(UUID meetingId, String authorizationHeader) {
        // 미팅 상세 조회 로직
        return null;
    }

    public ApiResponse<ParticipantsResponse> getParticipants(UUID meetingId, String authorizationHeader) {
        //미팅 참가자 목록 조회 로직
        return null;
    }

    public ApiResponse<ParticipationUpdateResponse> updateParticipation(UUID meetingId, ParticipationUpdateRequest request, String authorizationHeader) {
        //미팅 참여 정보 수정 로직
        return null;
    }
}
