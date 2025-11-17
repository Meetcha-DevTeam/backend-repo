package com.meetcha.meeting.controller;

import com.meetcha.global.annotation.AuthUser;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.meeting.dto.MeetingCreateRequest;
import com.meetcha.meeting.dto.MeetingCreateResponse;
import com.meetcha.meeting.dto.MeetingDeleteResponse;
import com.meetcha.meeting.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/meeting")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping("/create")
    public MeetingCreateResponse createMeeting(
            @RequestBody @Valid MeetingCreateRequest request,
            @AuthUser UUID userId
    ) {
        return meetingService.createMeeting(request, userId);
    }

    @DeleteMapping("/{meetingId}")
    public MeetingDeleteResponse deleteFailedMeeting(
            @PathVariable("meetingId") UUID meetingId,
            @AuthUser UUID userId
    ) {
        return meetingService.deleteFailedMeeting(meetingId, userId);
    }
}
