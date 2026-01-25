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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserScheduleService {

    private final GoogleTokenService googleTokenService;
    private final GoogleCalendarClient googleCalendarClient;

    // 유저 일정 조회
    public List<ScheduleResponse> getSchedule(UUID userId, LocalDateTime from, LocalDateTime to) {
        // 명세: 유효하지 않은 날짜 범위 → 400
        if (from == null || to == null || to.isBefore(from)) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE); // 없으면 제거하거나 enum에 추가
        }

        String accessToken = googleTokenService.ensureValidAccessToken(userId); // ⭐ 판단용 조회 범위 확장
        LocalDateTime inferFrom = from.minusMonths(1);
        LocalDateTime inferTo = to.plusMonths(1);
        // 1️⃣ 넓게 조회
        List<ScheduleResponse> raw = googleCalendarClient.getEvents(accessToken, inferFrom, inferTo);
        // 2️⃣ recurrence 판단
        List<ScheduleResponse> inferred = applyRecurrenceInference(raw);
        // 3️⃣ 원래 요청 범위로 다시 필터링
        return inferred.stream() .filter(s -> !s.getStartAt().isBefore(from) && !s.getStartAt().isAfter(to) ) .toList();
    }

    private List<ScheduleResponse> applyRecurrenceInference(List<ScheduleResponse> schedules) {

        List<ScheduleResponse> valid = schedules.stream()
                .filter(s -> s.getStartAt() != null)
                .toList();

        Map<String, List<ScheduleResponse>> dailyGroups =
                valid.stream()
                        .collect(Collectors.groupingBy(s ->
                                s.getTitle() + "|" + s.getStartAt().toLocalTime()
                        ));

        Map<String, List<ScheduleResponse>> weeklyGroups =
                valid.stream()
                        .collect(Collectors.groupingBy(s ->
                                s.getTitle()
                                        + "|" + s.getStartAt().getDayOfWeek()
                                        + "|" + s.getStartAt().toLocalTime()
                        ));

        Map<String, List<ScheduleResponse>> monthlyGroups =
                valid.stream()
                        .collect(Collectors.groupingBy(s ->
                                s.getTitle()
                                        + "|" + s.getStartAt().getDayOfMonth()
                                        + "|" + s.getStartAt().toLocalTime()
                        ));

        return valid.stream()
                .map(s -> {
                    String dailyKey = s.getTitle() + "|" + s.getStartAt().toLocalTime();
                    String weeklyKey = s.getTitle()
                            + "|" + s.getStartAt().getDayOfWeek()
                            + "|" + s.getStartAt().toLocalTime();
                    String monthlyKey = s.getTitle()
                            + "|" + s.getStartAt().getDayOfMonth()
                            + "|" + s.getStartAt().toLocalTime();

                    if (hasConsecutiveDays(dailyGroups.getOrDefault(dailyKey, List.of()))) {
                        return ScheduleResponse.builder()
                                .eventId(s.getEventId())
                                .title(s.getTitle())
                                .startAt(s.getStartAt())
                                .endAt(s.getEndAt())
                                .recurrence("DAILY")
                                .build();
                    }

                    if (monthlyGroups.getOrDefault(monthlyKey, List.of()).size() >= 2) {
                        return ScheduleResponse.builder()
                                .eventId(s.getEventId())
                                .title(s.getTitle())
                                .startAt(s.getStartAt())
                                .endAt(s.getEndAt())
                                .recurrence("MONTHLY")
                                .build();
                    }

                    if (weeklyGroups.getOrDefault(weeklyKey, List.of()).size() >= 2) {
                        return ScheduleResponse.builder()
                                .eventId(s.getEventId())
                                .title(s.getTitle())
                                .startAt(s.getStartAt())
                                .endAt(s.getEndAt())
                                .recurrence("WEEKLY")
                                .build();
                    }

                    return s;
                })
                .toList();
    }

    private boolean hasConsecutiveDays(List<ScheduleResponse> group) {
        if (group == null || group.size() < 3) return false; // 최소 3일

        List<LocalDateTime> dates = group.stream()
                .map(ScheduleResponse::getStartAt)
                .sorted()
                .toList();

        int streak = 1;

        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i - 1).toLocalDate().plusDays(1)
                    .equals(dates.get(i).toLocalDate())) {
                streak++;
                if (streak >= 3) return true;
            } else {
                streak = 1;
            }
        }
        return false;
    }

    // 유저 일정 생성
    public String createSchedule(UUID userId, CreateScheduleRequest request) {
        String recurrence = request.getRecurrence();
        if ("NONE".equalsIgnoreCase(recurrence) || recurrence == null || recurrence.isBlank()) {
            recurrence = null; // NONE, 빈 값 모두 null 처리
        }
        validateTimeSlot(request.getStartAt(), request.getEndAt());

        String accessToken = googleTokenService.ensureValidAccessToken(userId);
        String rrule = RecurrenceUtils.buildGoogleRRule(request.getRecurrence(), request.getStartAt());
        return googleCalendarClient.createEvent(
                accessToken,
                request.getTitle(),
                request.getStartAt(),
                request.getEndAt(),
                rrule
        );
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
