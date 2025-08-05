package com.meetcha.reflection.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetWrittenReflectionResponse {
    private UUID meetingId;
    private UUID projectId;        // nullable
    private String projectName;    // nullable
    private String title;
    private String confirmedTime;
    private String completedWork;  // nullable
    private String plannedWork;    // nullable

}
