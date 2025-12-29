/*package com.meetcha.user.service;

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
import org.springframework.web.client.HttpClientErrorException;

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
        try {
            return googleCalendarClient.getEvents(accessToken, from, to);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new CustomException(ErrorCode.MISSING_GOOGLE_ACCESS_TOKEN);
        }
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




}*/

package com.meetcha.user.service;

import com.meetcha.auth.service.GoogleTokenService;
import com.meetcha.external.google.GoogleCalendarClient;
import com.meetcha.external.google.RecurrenceUtils;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.user.dto.CreateScheduleRequest;
import com.meetcha.user.dto.ScheduleDetailResponse;
import com.meetcha.user.dto.ScheduleResponse;
import com.meetcha.user.dto.UpdateScheduleRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserScheduleService {

    private final GoogleTokenService googleTokenService;
    private final GoogleCalendarClient googleCalendarClient;

    // 유저 일정 조회
    public List<ScheduleResponse> getSchedule(UUID userId, LocalDateTime from, LocalDateTime to) {
        log.info("[일정 조회 요청] User ID: {}, From: {}, To: {}", userId, from, to);
        // 명세: 유효하지 않은 날짜 범위 → 400
        if (from == null || to == null || to.isBefore(from)) {
            log.warn("[일정 조회 실패] User ID: {}, 유효하지 않은 날짜 범위: {} ~ {}", userId, from, to);
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE); // 없으면 제거하거나 enum에 추가
        }

        String accessToken = googleTokenService.ensureValidAccessToken(userId);
        List<ScheduleResponse> result = googleCalendarClient.getEvents(accessToken, from, to);

        log.info("[일정 조회 성공] User ID: {}, 조회된 일정 수: {}", userId, result.size());
        return result;
    }

    // 유저 일정 생성
    public String createSchedule(UUID userId, CreateScheduleRequest request) {
        log.info("[일정 생성 시도] User ID: {}, Title: {}", userId, request.getTitle());

        String recurrence = request.getRecurrence();
        if ("NONE".equalsIgnoreCase(recurrence) || recurrence == null || recurrence.isBlank()) {
            recurrence = null; // NONE, 빈 값 모두 null 처리
        }
        validateTimeSlot(request.getStartAt(), request.getEndAt());

        String accessToken = googleTokenService.ensureValidAccessToken(userId);
        String rrule = RecurrenceUtils.buildGoogleRRule(request.getRecurrence(), request.getStartAt());

        String eventId = googleCalendarClient.createEvent(
                accessToken,
                request.getTitle(),
                request.getStartAt(),
                request.getEndAt(),
                rrule
        );

        log.info("[일정 생성 성공] User ID: {}, Event ID: {}", userId, eventId);
        return eventId;
    }

    // 유저 일정 수정
    public void updateSchedule(UUID userId, UpdateScheduleRequest request) {
        validateTimeSlot(request.getStartAt(), request.getEndAt());

        String accessToken = googleTokenService.ensureValidAccessToken(userId);
        googleCalendarClient.updateEvent(
                accessToken,
                request.getEventId(),
                request.getTitle(),
                request.getStartAt(),
                request.getEndAt(),
                request.getRecurrence()
        );
    }

    // 유저 일정 삭제
    public void deleteSchedule(UUID userId, String eventId) {
        String accessToken = googleTokenService.ensureValidAccessToken(userId);
        googleCalendarClient.deleteEvent(accessToken, eventId);
    }

    // 단일 상세 일정 조회
    public ScheduleDetailResponse getScheduleDetail(UUID userId, String eventId) {
        String accessToken = googleTokenService.ensureValidAccessToken(userId);
        return googleCalendarClient.getEventById(accessToken, eventId);
    }

    private void validateTimeSlot(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt.isAfter(endAt)) {
            throw new CustomException(ErrorCode.INVALID_TIME_SLOT);
        }
    }
}
