package com.meetcha.user.controller;

import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.annotation.AuthUser;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.util.AuthHeaderUtils;
import com.meetcha.user.dto.*;
import com.meetcha.user.service.UserScheduleService;
import com.meetcha.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserScheduleController {

    private final UserProfileService userProfileService;
    private final UserScheduleService userScheduleService;


    //유저 스케줄 조회
    @GetMapping("/schedule")
    public List<ScheduleResponse> getSchedule(
            @AuthUser UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return userScheduleService.getSchedule(userId, from, to);
    }

    // 유저 개인 일정 Google Calendar에 등록
    @PostMapping("/schedule/create")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateScheduleResponse createSchedule(
                                                  @AuthUser UUID userId,
                                                  @RequestBody CreateScheduleRequest request
    ) {
        String id = userScheduleService.createSchedule(userId, request);

        return new CreateScheduleResponse(id);
    }
    

    // 유저 개인 일정 수정
    @PutMapping("/schedule/update")
    public void updateSchedule(
            @AuthUser UUID userId,
            @RequestBody UpdateScheduleRequest request
    ) {
        userScheduleService.updateSchedule(userId, request);
    }

    // 단일 상세 일정 조회
    @GetMapping("/schedule/detail")
    public ScheduleDetailResponse getScheduleDetail(
            @AuthUser UUID userId,
            @RequestParam String eventId) {
        return userScheduleService.getScheduleDetail(userId, eventId);
    }

    // 유저 개인 일정 삭제
    @DeleteMapping("/schedule/delete")
    public void deleteSchedule(
            @AuthUser UUID userId,
            @RequestParam String eventId) {
        userScheduleService.deleteSchedule(userId, eventId);
    }

    //유저 정보 조회(마이페이지)
    @GetMapping("/mypage")
    public MyPageResponse getMyPage(
            @AuthUser UUID userId
    ) {
        return userProfileService.getMyPage(userId);}

}
