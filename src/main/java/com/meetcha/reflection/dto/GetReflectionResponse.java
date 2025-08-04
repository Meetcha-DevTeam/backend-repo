package com.meetcha.reflection.dto;

import lombok.Builder;

import java.util.UUID;
@Builder
public record GetReflectionResponse (
        UUID meetingId,
        UUID projectId,            //nullable
        String projectName,        //nullable
        String title,
        String description,
        String date,
        int contribution,
        String role,
        String thought,
        String completedWork,     // nullable
        String plannedWork        // nullable
) {}