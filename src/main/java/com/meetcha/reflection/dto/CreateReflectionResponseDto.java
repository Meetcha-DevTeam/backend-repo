package com.meetcha.reflection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
 //회고 생성 후 응답
public class CreateReflectionResponseDto {
  UUID reflectionId;
}