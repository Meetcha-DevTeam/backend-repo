package com.meetcha.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateProjectRequest {
    @NotBlank(message = "프로젝트 이름은 공백일 수 없습니다.")
    private String name;
}
