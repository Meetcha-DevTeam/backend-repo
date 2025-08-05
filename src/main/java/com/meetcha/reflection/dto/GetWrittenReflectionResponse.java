package com.meetcha.reflection.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class GetWrittenReflectionResponse {
    private UUID meetingId;
    private UUID projectId;        // nullable
    private String projectName;    // nullable
    private String title;
    private String confirmedTime;
    private String completedWork;  // nullable
    private String plannedWork;    // nullable

    //JPQL에서 confirmedTime을 String으로 변환하므로, 생성자 명시 필요
    public GetWrittenReflectionResponse(
            UUID meetingId,
            UUID projectId,
            String projectName,
            String title,
            String confirmedTime,
            String completedWork,
            String plannedWork
    ) {
        this.meetingId = meetingId;
        this.projectId = projectId;
        this.projectName = projectName;
        this.title = title;
        this.confirmedTime = confirmedTime;
        this.completedWork = completedWork;
        this.plannedWork = plannedWork;
    }
}
