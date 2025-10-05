package com.meetcha.reflection.controller;

import com.meetcha.global.annotation.AuthUser;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.reflection.dto.CreateReflectionRequestDto;
import com.meetcha.reflection.dto.CreateReflectionResponseDto;
import com.meetcha.reflection.dto.GetReflectionResponse;
import com.meetcha.reflection.dto.GetWrittenReflectionResponse;
import com.meetcha.reflection.service.MeetingReflectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meeting")
public class MeetingReflectionController {

    private final MeetingReflectionService reflectionService;

    //회고 생성
    @PostMapping("/{meetingId}/reflection/create")
    public CreateReflectionResponseDto createReflection(
            @PathVariable UUID meetingId,
            @RequestBody CreateReflectionRequestDto requestDto,
            @AuthUser UUID userId
    ) {
        return reflectionService.createReflection(userId, meetingId, requestDto);
    }

        //미팅 회고 목록 요약 조회
    @GetMapping("/reflections")
    public List<GetWrittenReflectionResponse> getWrittenReflections(@AuthUser UUID userId
    ) {
        return reflectionService.getWrittenReflections(userId);
    }

    //특정 회고 조회
    @GetMapping("/{meetingId}/reflection")
    public GetReflectionResponse getReflectionDetail(
            @PathVariable UUID meetingId,
            @AuthUser UUID userId
    ) {
        return reflectionService.getReflectionDetail(userId, meetingId);
    }
}