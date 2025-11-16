package com.meetcha.joinmeeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateMeetingCodeResponse{
        UUID meetingId;
        String title;
        String description;
        LocalDateTime deadline;
        Boolean isClosed;
}
