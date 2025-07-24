package com.meetcha.meetinglist.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ParticipantDto {
    private UUID participantId;
    private String nickname;
    private String profileImageUrl; // 예: S3 저장 URL 등
}
