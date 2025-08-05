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

    //JPQL로 LocalDateTime을 매핑받을 때 문자열 포맷("yyyy-MM-dd HH:mm:ss")으로 변환하기 위해 생성자에서 직접 처리
    //Hibernate는 LocalDateTime → String 자동 변환이 불가능하므로 생성자 필수
    public GetWrittenReflectionResponse(
            UUID meetingId,
            UUID projectId,
            String projectName,
            String title,
            LocalDateTime confirmedTime,
            String completedWork,
            String plannedWork
    ) {
        this.meetingId = meetingId;
        this.projectId = projectId;
        this.projectName = projectName;
        this.title = title;
        this.confirmedTime = confirmedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.completedWork = completedWork;
        this.plannedWork = plannedWork;
    }
}
