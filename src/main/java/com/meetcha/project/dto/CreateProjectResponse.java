package com.meetcha.project.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CreateProjectResponse {
    private UUID projectId;
    private String name;
    private LocalDateTime createdAt;
}
