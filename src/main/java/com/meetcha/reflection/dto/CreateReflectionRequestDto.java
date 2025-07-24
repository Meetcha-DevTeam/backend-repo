package com.meetcha.reflection.dto;

import lombok.Getter;

import java.util.UUID;

 //회고 생성 요청 DTO
 //프론트로 전달되는 정보
@Getter
public class CreateReflectionRequestDto {
    private int contribution;        //기여도 (0~100)
    private String role;             //맡은 역할
    private String thought;          //느낀점
    private String completedWork;    //한 일 (선택)
    private String plannedWork;      //할 일 (선택)
    private UUID projectId;          //프로젝트 ID (선택)
}
