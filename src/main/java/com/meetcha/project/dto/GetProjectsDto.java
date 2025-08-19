package com.meetcha.project.dto;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
//프론트에 전달할 프로젝트 요약 정보 DTO
public class GetProjectsDto {
    private UUID projectId;
    private String projectName;

    //JPQL new에서 사용할 수 있도록 public으로 명시(@AllArgsConstructor은 오류남)
    public GetProjectsDto(UUID projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
    }
}