package com.meetcha.user.controller;

import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.util.AuthHeaderUtils;
import com.meetcha.user.dto.*;
import com.meetcha.user.service.UserScheduleService;
import com.meetcha.user.service.UserProfileService;
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
    private final JwtProvider jwtProvider;

    public record IdResponse(String eventId) {}

    //유저 스케줄 조회
    @GetMapping("/schedule")
    public List<ScheduleResponse> getSchedule(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        String bearer = AuthHeaderUtils.extractBearerToken(authorizationHeader);
        String masked = (bearer != null && bearer.length() > 16)
                ? bearer.substring(0, 10) + "..." + bearer.substring(bearer.length()-6)
                : "null";

        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));
        return userScheduleService.getSchedule(userId, from, to);
    }

    // 유저 개인 일정 Google Calendar에 등록
    @PostMapping("/schedule/create")
    public ResponseEntity<ApiResponse<IdResponse>> createSchedule(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody CreateScheduleRequest request
    ) {
        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));
        String id = userScheduleService.createSchedule(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("/user/schedule/create", 201, "CREATED", new IdResponse(id)));
    }

//    public String createSchedule(
//            @RequestHeader("Authorization") String authorizationHeader,
//            @RequestBody CreateScheduleRequest request
//    ) {
//        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));
//        return userScheduleService.createSchedule(userId, request);
//    }


    // 유저 개인 일정 수정
    @PutMapping("/schedule/update")
    public void updateSchedule(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody UpdateScheduleRequest request
    ) {
        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));
        userScheduleService.updateSchedule(userId, request);
    }

    // 단일 상세 일정 조회
    @GetMapping("/schedule/detail")
    public ScheduleDetailResponse getScheduleDetail(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam String eventId) {
        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));
        return userScheduleService.getScheduleDetail(userId, eventId);
    }

    // 유저 개인 일정 삭제
    @DeleteMapping("/schedule/delete")
    public void deleteSchedule(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam String eventId) {
        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));
        userScheduleService.deleteSchedule(userId, eventId);
    }

    //유저 정보 조회(마이페이지)
    @GetMapping("/mypage")
    public MyPageResponse getMyPage(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));
        return userProfileService.getMyPage(userId);}

}
