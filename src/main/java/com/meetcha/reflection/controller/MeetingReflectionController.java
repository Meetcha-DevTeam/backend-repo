package com.meetcha.reflection.controller;

import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.exception.UnauthorizedException;
import com.meetcha.reflection.dto.CreateReflectionRequestDto;
import com.meetcha.reflection.dto.CreateReflectionResponseDto;
import com.meetcha.reflection.service.MeetingReflectionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meeting")
public class MeetingReflectionController {

    private final MeetingReflectionService reflectionService;
    private final JwtProvider jwtProvider;

    @PostMapping("/{meetingId}/reflection/create")
    public ResponseEntity<ApiResponse<CreateReflectionResponseDto>> createReflection(
            @PathVariable UUID meetingId,
            @RequestBody CreateReflectionRequestDto requestDto,
            HttpServletRequest request
    ) {
        UUID userId = extractUserIdFromToken(request);

        CreateReflectionResponseDto response = reflectionService.createReflection(userId, meetingId, requestDto);
        return ResponseEntity
                .status(201)
                .body(ApiResponse.success(201, "회고가 성공적으로 작성되었습니다.", response));
    }

     //JWT 토큰에서 userId 추출
    private UUID extractUserIdFromToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            String token = bearer.substring(7);
            return jwtProvider.getUserId(token);
        }
        throw new UnauthorizedException(ErrorCode.MISSING_AUTH_TOKEN);
    }
}
