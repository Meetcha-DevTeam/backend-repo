package com.meetcha.joinmeeting.dto;
import java.util.UUID;

public record JoinMeetingResponse(
        //	POST /meetings/{id}/join 요청 바디 DTO
        UUID meetingId,
        UUID participantId
) {}