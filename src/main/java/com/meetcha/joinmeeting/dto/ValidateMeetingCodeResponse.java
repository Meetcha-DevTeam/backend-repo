package com.meetcha.joinmeeting.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ValidateMeetingCodeResponse(
        UUID meetingId,
        String title,
        String description,
        LocalDateTime deadline,
        boolean isClosed
) {}
