package com.meetcha.joinmeeting.dto;

import com.meetcha.joinmeeting.domain.MeetingParticipant;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class MeetingParticipantDto {
    private UUID participantId;
    private String nickname;
    private String profileImageUrl;
}