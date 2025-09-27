package com.meetcha.reflection.controller;

import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.util.AuthHeaderUtils;
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
    private final JwtProvider jwtProvider;

    //회고 생성
    @PostMapping("/{meetingId}/reflection/create")
    public CreateReflectionResponseDto createReflection(
            @PathVariable UUID meetingId,
            @RequestBody CreateReflectionRequestDto requestDto,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));
        return reflectionService.createReflection(userId, meetingId, requestDto);
    }

        //미팅 회고 목록 요약 조회
    @GetMapping("/reflections")
    public List<GetWrittenReflectionResponse> getWrittenReflections(@RequestHeader("Authorization") String authorizationHeader
    ) {
        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));
        return reflectionService.getWrittenReflections(userId);

    }

    //특정 회고 조회
    @GetMapping("/{meetingId}/reflection")
    public GetReflectionResponse getReflectionDetail(
            @PathVariable UUID meetingId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));
        return reflectionService.getReflectionDetail(userId, meetingId);
    }

}
