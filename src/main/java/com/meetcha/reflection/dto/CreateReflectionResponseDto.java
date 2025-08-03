package com.meetcha.reflection.dto;

import java.util.UUID;

 //회고 생성 후 응답
public record CreateReflectionResponseDto(
        UUID reflectionId
) {}
