package com.meetcha.user.service;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.external.google.GoogleCalendarClient;
import com.meetcha.external.google.RecurrenceUtils;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.user.dto.CreateScheduleRequest;
import com.meetcha.user.dto.ScheduleDetailResponse;
import com.meetcha.user.dto.UpdateScheduleRequest;
import com.meetcha.user.dto.ScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserScheduleService {

    private final UserRepository userRepository; // 유저의 access token 조회용
    private final GoogleCalendarClient googleCalendarClient; // 구글 캘린더 호출용

    //유저 일정 조회
    public List<ScheduleResponse> getSchedule(UUID userId, LocalDateTime from, LocalDateTime to) {
        // 유저 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // access token 가져오기
        String accessToken = user.getGoogleToken();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new CustomException(ErrorCode.MISSING_GOOGLE_ACCESS_TOKEN);
        }

        // Google Calendar API로 일정 조회
        return googleCalendarClient.getEvents(accessToken, from, to);
    }


    //유저 일정 생성
    public String createSchedule(UUID userId, CreateScheduleRequest request) {
        // 유저 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 토큰 확인
        String accessToken = user.getGoogleToken();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new CustomException(ErrorCode.MISSING_GOOGLE_ACCESS_TOKEN);
        }

        String rrule = RecurrenceUtils.buildGoogleRRule(request.recurrence(), request.startAt());

        // Google Calendar에 이벤트 생성 요청
        return googleCalendarClient.createEvent(
                accessToken,
                request.title(),
                request.startAt(),
                request.endAt(),
                rrule
        );
    }

    //유저 일정 수정
    public void updateSchedule(UUID userId, UpdateScheduleRequest request) {
        // 유저 조회 및 토큰 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String accessToken = user.getGoogleToken();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new CustomException(ErrorCode.MISSING_GOOGLE_ACCESS_TOKEN);
        }

        // Google Calendar에 일정 수정 요청
        googleCalendarClient.updateEvent(
                accessToken,
                request.eventId(),
                request.title(),
                request.startAt(),
                request.endAt()
        );
    }

    //유저 일정 삭제
    public void deleteSchedule(UUID userId, String eventId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String accessToken = user.getGoogleToken();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new CustomException(ErrorCode.MISSING_GOOGLE_ACCESS_TOKEN);
        }

        googleCalendarClient.deleteEvent(accessToken, eventId);
    }



    // 단일 상세 일정 조회
    public ScheduleDetailResponse getScheduleDetail(UUID userId, String eventId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String accessToken = user.getGoogleToken();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new CustomException(ErrorCode.MISSING_GOOGLE_ACCESS_TOKEN);
        }

        return googleCalendarClient.getEventById(accessToken, eventId);
    }




}

