package com.meetcha.user.controller;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserScheduleController {

    private final UserScheduleService userScheduleService;

    //유저 스케줄 조회
    @GetMapping("/schedule")
    public ApiResponse<List<scheduleResponse>> getSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        UUID userId = getCurrentUserId();
        List<scheduleResponse> schedules = userScheduleService.getSchedule(userId, from, to);
        return ApiResponse.success(200, "유저 스케줄 조회 성공", schedules);
    }

    // 유저 개인 일정 Google Calendar에 등록
    @PostMapping("/schedule/create")
    public ApiResponse<String> createSchedule(
            @RequestBody CreateScheduleRequest request
    ) {
        UUID userId = getCurrentUserId();//todo
        String eventId = userScheduleService.createSchedule(userId, request);
        return ApiResponse.success(201, "일정 생성 성공", eventId);
    }


    // SecurityContextHolder에서 현재 userId 추출
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }
        return (UUID) authentication.getPrincipal();
    }

    // 유저 개인 일정 수정
    @PutMapping("/schedule/update")
    public ApiResponse<Void> updateSchedule(
            @RequestBody UpdateScheduleRequest request
    ) {
        UUID userId = getCurrentUserId(); // todo JWT 기반 추출 예정
        userScheduleService.updateSchedule(userId, request);
        return ApiResponse.success(200, "일정 수정 성공");
    }

    // 단일 상세 일정 조회
    @GetMapping("/schedule/detail")
    public ApiResponse<ScheduleDetailResponse> getScheduleDetail(@RequestParam String eventId) {
        UUID userId = getCurrentUserId(); // todo JWT 기반 추출 예정
        ScheduleDetailResponse detail = userScheduleService.getScheduleDetail(userId, eventId);
        return ApiResponse.success(200, "일정 상세 조회 성공", detail);
    }

    // 유저 개인 일정 삭제
    @DeleteMapping("/schedule/delete")
    public ApiResponse<Void> deleteSchedule(@RequestParam String eventId) {
        UUID userId = getCurrentUserId(); // todo JWT 기반 추출 예정
        userScheduleService.deleteSchedule(userId, eventId);
        return ApiResponse.success(200, "일정 삭제 성공");
    }



}
