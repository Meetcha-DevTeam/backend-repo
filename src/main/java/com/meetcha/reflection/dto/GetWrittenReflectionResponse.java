package com.meetcha.reflection.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class GetWrittenReflectionResponse {
    private UUID meetingId;
    private UUID projectId;
    private String projectName;
    private String title;
    private String confirmedTime;
    private String completedWork;
    private String plannedWork;

    public GetWrittenReflectionResponse(
            UUID meetingId,
            UUID projectId,
            String projectName,
            String title,
            LocalDateTime confirmedTime, // 여기 수정
            String completedWork,
            String plannedWork
    ) {
        this.meetingId = meetingId;
        this.projectId = projectId;
        this.projectName = projectName;
        this.title = title;
        this.confirmedTime = confirmedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); // 문자열로 포맷
        this.completedWork = completedWork;
        this.plannedWork = plannedWork;
    }
}
