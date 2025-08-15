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
    public ResponseEntity<ApiResponse<CreateReflectionResponseDto>> createReflection(
            @PathVariable UUID meetingId,
            @RequestBody CreateReflectionRequestDto requestDto,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));

        CreateReflectionResponseDto response = reflectionService.createReflection(userId, meetingId, requestDto);
        return ResponseEntity
                .status(201)
                .body(ApiResponse.success(201, "회고가 성공적으로 작성되었습니다.", response));
    }

    //미팅 회고 목록 요약 조회
    @GetMapping("/reflections")
    public ResponseEntity<ApiResponse<List<GetWrittenReflectionResponse>>> getWrittenReflections(@RequestHeader("Authorization") String authorizationHeader
    ) {
        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));

        List<GetWrittenReflectionResponse> responses = reflectionService.getWrittenReflections(userId);

        return ResponseEntity.ok(ApiResponse.success(200, "회고 조회 성공", responses));
    }

    //특정 회고 조회
    @GetMapping("/{meetingId}/reflection")
    public ResponseEntity<ApiResponse<GetReflectionResponse>> getReflectionDetail(
            @PathVariable UUID meetingId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));

        GetReflectionResponse response = reflectionService.getReflectionDetail(userId, meetingId);

        return ResponseEntity.ok(ApiResponse.success(200, "회고 상세 조회에 성공했습니다.", response));
    }

}
