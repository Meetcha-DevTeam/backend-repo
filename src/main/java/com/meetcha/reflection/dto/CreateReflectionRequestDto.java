package com.meetcha.reflection.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.UUID;

 //회고 생성 요청 DTO
@Getter
public class CreateReflectionRequestDto {
     @Min(value = 0, message = "기여도는 0 이상이어야 합니다.")
     @Max(value = 100, message = "기여도는 100 이하여야 합니다.")
     private int contribution;        //기여도 (0~100)
     @NotBlank(message = "역할은 필수 항목입니다.")
     private String role;             //맡은 역할
     private String thought;          //느낀점
     private String completedWork;    //한 일 (선택)
     private String plannedWork;      //할 일 (선택)
     private UUID projectId;          //프로젝트 ID (선택)
}
