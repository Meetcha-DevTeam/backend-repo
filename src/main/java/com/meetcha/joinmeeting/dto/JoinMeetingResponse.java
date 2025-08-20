package com.meetcha.joinmeeting.dto;
import java.util.UUID;

public record JoinMeetingResponse(
        UUID meetingId,
        UUID participantId
) {}