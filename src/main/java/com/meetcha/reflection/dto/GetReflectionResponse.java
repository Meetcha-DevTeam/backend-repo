package com.meetcha.reflection.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
public class GetReflectionResponse {
    private final UUID meetingId;
    private final UUID projectId;
    private final String projectName;
    private final String title;
    private final String description;
    private final String confirmedTime;
    private final int contribution;
    private final String role;
    private final String thought;
    private final String completedWork;
    private final String plannedWork;

    //JPQL로 LocalDateTime을 매핑받을 때 문자열 포맷("yyyy-MM-dd HH:mm:ss")으로 변환하기 위해 생성자에서 직접 처리
    //Hibernate는 LocalDateTime → String 자동 변환이 불가능하므로 생성자 필수
    public GetReflectionResponse(
            UUID meetingId,
            UUID projectId,
            String projectName,
            String title,
            String description,
            LocalDateTime confirmedTime,
            int contribution,
            String role,
            String thought,
            String completedWork,
            String plannedWork
    ) {
        this.meetingId = meetingId;
        this.projectId = projectId;
        this.projectName = projectName;
        this.title = title;
        this.description = description;
        this.confirmedTime = confirmedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.contribution = contribution;
        this.role = role;
        this.thought = thought;
        this.completedWork = completedWork;
        this.plannedWork = plannedWork;
    }

}
