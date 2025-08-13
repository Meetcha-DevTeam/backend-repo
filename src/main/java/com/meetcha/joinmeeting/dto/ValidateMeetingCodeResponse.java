package com.meetcha.joinmeeting.dto;

import java.util.UUID;

public record ValidateMeetingCodeResponse(
        UUID meetingId,
        String title,
        String description,
        String deadline,
        boolean isClosed
) {}
