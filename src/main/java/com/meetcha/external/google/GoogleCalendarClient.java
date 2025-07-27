package com.meetcha.external.google;

import com.meetcha.user.dto.ScheduleDetailResponse;
import com.meetcha.user.dto.scheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GoogleCalendarClient {

    private final RestTemplate restTemplate;
    // accessToken과 기간을 받아 유저의 Google Calendar 일정을 조회
    public List<scheduleResponse> getEvents(String accessToken, LocalDateTime from, LocalDateTime to) {
        // Authorization 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        // HTTP 요청 객체 생성
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Google Calendar API 요청 URL 구성
        String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/calendar/v3/calendars/primary/events")
                .queryParam("timeMin", from + "Z")
                .queryParam("timeMax", to + "Z")
                .queryParam("singleEvents", "true")
                .queryParam("orderBy", "startTime")
                .build()
                .toString();

        // Google Calendar API에 GET 요청
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

        // 이벤트 목록 추출
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");

        // 이벤트 → scheduleResponse 변환
        return items.stream()
                .map(this::toScheduleResponse)
                .collect(Collectors.toList());
    }

    // 이벤트 데이터를 scheduleResponse로 변환
    private scheduleResponse toScheduleResponse(Map<String, Object> item) {
        Map<String, String> start = (Map<String, String>) item.get("start");
        Map<String, String> end = (Map<String, String>) item.get("end");

        String title = (String) item.getOrDefault("summary", "제목 없음"); // 제목이 없는 경우 대체 텍스트
        String eventId = (String) item.get("id"); // 제목이 없는 경우 대체 텍스트

        // recurrence 파싱
        List<String> recurrenceList = (List<String>) item.get("recurrence");
        String recurrence = RecurrenceUtils.parseRecurrenceToLabel(recurrenceList);

        return new scheduleResponse(
                eventId,
                title,
                LocalDateTime.parse(start.get("dateTime")),
                LocalDateTime.parse(end.get("dateTime")),
                recurrence
        );
    }

    //일정 생성
    public String createEvent(String accessToken, String title, LocalDateTime startAt, LocalDateTime endAt, String recurrenceRRule) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 바디 구성: Google Calendar API에 맞춘 이벤트 데이터
        Map<String, Object> body = new HashMap<>();
        body.put("summary", title);

        Map<String, String> start = Map.of("dateTime", startAt.toString(), "timeZone", "Asia/Seoul");
        Map<String, String> end = Map.of("dateTime", endAt.toString(), "timeZone", "Asia/Seoul");

        body.put("start", start);
        body.put("end", end);

        if (recurrenceRRule != null) {
            body.put("recurrence", List.of(recurrenceRRule));
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Google Calendar API에 POST 요청 → primary 캘린더에 이벤트 생성
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://www.googleapis.com/calendar/v3/calendars/primary/events",
                HttpMethod.POST,
                request,
                Map.class
        );

        // 응답에서 Google Calendar의 eventId 추출하여 반환
        return (String) response.getBody().get("id");
    }

    //일정 수정
    public void updateEvent(String accessToken, String eventId, String title, LocalDateTime startAt, LocalDateTime endAt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("summary", title);

        Map<String, String> start = Map.of(
                "dateTime", startAt.toString(),
                "timeZone", "Asia/Seoul"
        );
        Map<String, String> end = Map.of(
                "dateTime", endAt.toString(),
                "timeZone", "Asia/Seoul"
        );

        body.put("start", start);
        body.put("end", end);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // PATCH 요청으로 기존 이벤트 수정
        restTemplate.exchange(
                "https://www.googleapis.com/calendar/v3/calendars/primary/events/" + eventId,
                HttpMethod.PATCH,
                request,
                Void.class
        );
    }

    //일정 삭제
    public void deleteEvent(String accessToken, String eventId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        restTemplate.exchange(
                "https://www.googleapis.com/calendar/v3/calendars/primary/events/" + eventId,
                HttpMethod.DELETE,
                request,
                Void.class
        );
    }


    // 단일 상세 일정 조회
    public ScheduleDetailResponse getEventById(String accessToken, String eventId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://www.googleapis.com/calendar/v3/calendars/primary/events/" + eventId,
                HttpMethod.GET,
                request,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        Map<String, String> start = (Map<String, String>) body.get("start");
        Map<String, String> end = (Map<String, String>) body.get("end");

        // recurrence 파싱
        List<String> recurrenceList = (List<String>) body.get("recurrence");
        String recurrence = RecurrenceUtils.parseRecurrenceToLabel(recurrenceList);

        return new ScheduleDetailResponse(
                (String) body.get("id"),
                (String) body.get("summary"),
                LocalDateTime.parse(start.get("dateTime")),
                LocalDateTime.parse(end.get("dateTime")),
                recurrence
        );
    }


}
