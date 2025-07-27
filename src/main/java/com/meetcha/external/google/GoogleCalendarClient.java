package com.meetcha.external.google;

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

        return new scheduleResponse(
                LocalDateTime.parse(start.get("dateTime")),
                LocalDateTime.parse(end.get("dateTime"))
        );
    }
}
