package com.meetcha.user.controller;

import com.meetcha.user.dto.BusyTimeResponse;
import com.meetcha.user.service.UserScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserScheduleController {

    private final UserScheduleService userScheduleService;

    //유저 스케줄 조회
    @GetMapping("/user/me/busy-times")
    public List<BusyTimeResponse> getBusyTimes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        UUID userId = getCurrentUserId();
        return userScheduleService.getBusyTimes(userId, from, to);
    }

    private UUID getCurrentUserId() {
        // todo 이후 연결하기(실제 로그인 상태에서 JWT를 파싱해 userId 추출)
        return UUID.fromString("11111111-1111-1111-1111-111111111111");
    }
}
