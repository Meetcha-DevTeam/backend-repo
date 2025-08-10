package com.meetcha.user.controller;

import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.user.dto.CreateScheduleRequest;
import com.meetcha.user.dto.ScheduleDetailResponse;
import com.meetcha.user.dto.UpdateScheduleRequest;
import com.meetcha.user.dto.scheduleResponse;
import com.meetcha.user.service.UserScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserScheduleController {

    private final UserScheduleService userScheduleService;
    private final JwtProvider jwtProvider;

    //유저 스케줄 조회
    @GetMapping("/schedule")
    public ApiResponse<List<scheduleResponse>> getSchedule(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        UUID userId = jwtProvider.getUserId(extractBearerToken(authorizationHeader));
        List<scheduleResponse> schedules = userScheduleService.getSchedule(userId, from, to);
        return ApiResponse.success(200, "유저 스케줄 조회 성공", schedules);
    }

    // 유저 개인 일정 Google Calendar에 등록
    @PostMapping("/schedule/create")
    public ApiResponse<String> createSchedule(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody CreateScheduleRequest request
    ) {
        UUID userId = jwtProvider.getUserId(extractBearerToken(authorizationHeader));
        String eventId = userScheduleService.createSchedule(userId, request);
        return ApiResponse.success(201, "일정 생성 성공", eventId);
    }


    // 유저 개인 일정 수정
    @PutMapping("/schedule/update")
    public ApiResponse<Void> updateSchedule(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody UpdateScheduleRequest request
    ) {
        UUID userId = jwtProvider.getUserId(extractBearerToken(authorizationHeader));
        userScheduleService.updateSchedule(userId, request);
        return ApiResponse.success(200, "일정 수정 성공");
    }

    // 단일 상세 일정 조회
    @GetMapping("/schedule/detail")
    public ApiResponse<ScheduleDetailResponse> getScheduleDetail(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam String eventId) {
        UUID userId = jwtProvider.getUserId(extractBearerToken(authorizationHeader));
        ScheduleDetailResponse detail = userScheduleService.getScheduleDetail(userId, eventId);
        return ApiResponse.success(200, "일정 상세 조회 성공", detail);
    }

    // 유저 개인 일정 삭제
    @DeleteMapping("/schedule/delete")
    public ApiResponse<Void> deleteSchedule(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam String eventId) {
        UUID userId = jwtProvider.getUserId(extractBearerToken(authorizationHeader));
        userScheduleService.deleteSchedule(userId, eventId);
        return ApiResponse.success(200, "일정 삭제 성공");
    }


    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null) throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        String h = authorizationHeader.trim();
        if (h.toLowerCase().startsWith("bearer ")) return h.substring(7).trim();
        throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
    }

}
