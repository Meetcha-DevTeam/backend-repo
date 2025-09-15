package com.meetcha.joinmeeting.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinMeetingResponse {
    UUID meetingId;
    UUID participantId;
}
