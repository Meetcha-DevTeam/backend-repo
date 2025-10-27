package com.meetcha.external.google;

import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.user.dto.ScheduleDetailResponse;
import com.meetcha.user.dto.ScheduleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarClient {

    private static final String BASE = "https://www.googleapis.com/calendar/v3/calendars/primary/events";
    private static final ZoneId Z_SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter RFC3339_UTC = DateTimeFormatter.ISO_INSTANT; // e.g. 2025-08-01T05:00:00Z
    private static final DateTimeFormatter RFC3339_OFFSET = DateTimeFormatter.ISO_OFFSET_DATE_TIME; // e.g. 2025-08-01T14:00:00+09:00

    private final RestTemplate restTemplate;

    // 일정 조회
    public List<ScheduleResponse> getEvents(String accessToken, LocalDateTime from, LocalDateTime to) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        /*// 로컬 -> UTC(RFC3339)로 변환
        Instant fromUtc = from.atZone(ZoneId.systemDefault()).toInstant();
        Instant toUtc   = to.atZone(ZoneId.systemDefault()).toInstant();

        String pageToken = null;
        List<Map<String, Object>> allItems = new ArrayList<>();

        try {
            do {
                UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(BASE)
                        .queryParam("timeMin", RFC3339_UTC.format(fromUtc))
                        .queryParam("timeMax", RFC3339_UTC.format(toUtc))
                        .queryParam("singleEvents", "true")
                        .queryParam("orderBy", "startTime");
                if (pageToken != null) b.queryParam("pageToken", pageToken);

                String url = b.build(true).toUriString();

                ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
                Map body = res.getBody();
                if (body == null) break;

                List<Map<String, Object>> items = (List<Map<String, Object>>) body.getOrDefault("items", List.of());
                allItems.addAll(items);

                pageToken = (String) body.get("nextPageToken");
            } while (pageToken != null);

        } catch (HttpClientErrorException.Forbidden e) {
            String msg = e.getResponseBodyAsString();
            if (msg != null && msg.contains("insufficientPermissions")) {
                throw new CustomException(ErrorCode.GOOGLE_SCOPE_INSUFFICIENT);
            }
            throw e;
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new CustomException(ErrorCode.GOOGLE_TOKEN_EXPIRED);
        }

        return allItems.stream().map(this::toScheduleResponse).collect(Collectors.toList());*/
        // LocalDateTime(Asia/Seoul) -> UTC OffsetDateTime (RFC3339)
        OffsetDateTime fromUtc = from.atZone(Z_SEOUL)
                .toOffsetDateTime()
                .withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime toUtc = to.atZone(Z_SEOUL)
                .toOffsetDateTime()
                .withOffsetSameInstant(ZoneOffset.UTC);

        String pageToken = null;
        List<Map<String, Object>> allItems = new ArrayList<>();

        try {
            do {
                UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(BASE)
                        .queryParam("timeMin", fromUtc.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .queryParam("timeMax", toUtc.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .queryParam("singleEvents", "true")
                        .queryParam("orderBy", "startTime");
                if (pageToken != null) b.queryParam("pageToken", pageToken);

                String url = b.build(true).toUriString();
                ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
                Map body = res.getBody();
                if (body == null) break;

                List<Map<String, Object>> items = (List<Map<String, Object>>) body.getOrDefault("items", List.of());
                allItems.addAll(items);

                pageToken = (String) body.get("nextPageToken");
            } while (pageToken != null);

        } catch (HttpClientErrorException.Forbidden e) {
            String msg = e.getResponseBodyAsString();
            if (msg != null && msg.contains("insufficientPermissions")) {
                throw new CustomException(ErrorCode.GOOGLE_SCOPE_INSUFFICIENT);
            }
            throw e;
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new CustomException(ErrorCode.GOOGLE_TOKEN_EXPIRED);
        }

        return allItems.stream().map(this::toScheduleResponse).collect(Collectors.toList());
    }

    // 일정 생성
    public String createEvent(String accessToken, String title, LocalDateTime startAt, LocalDateTime endAt, String recurrenceRRule) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("summary", title);

        // 오프셋 포함 RFC3339로 전송 (Asia/Seoul)
        String startStr = startAt.atZone(Z_SEOUL).format(RFC3339_OFFSET);
        String endStr = endAt.atZone(Z_SEOUL).format(RFC3339_OFFSET);

        body.put("start", Map.of("dateTime", startStr, "timeZone", Z_SEOUL.getId()));
        body.put("end", Map.of("dateTime", endStr, "timeZone", Z_SEOUL.getId()));

        if (recurrenceRRule != null) {
            body.put("recurrence", List.of(recurrenceRRule));
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(BASE, HttpMethod.POST, request, Map.class);
            return (String) response.getBody().get("id");
        } catch (HttpClientErrorException.Forbidden e) {
            String msg = e.getResponseBodyAsString();
            if (msg != null && msg.contains("insufficientPermissions")) {
                throw new CustomException(ErrorCode.GOOGLE_SCOPE_INSUFFICIENT);
            }
            throw e;
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new CustomException(ErrorCode.GOOGLE_TOKEN_EXPIRED);
        }
    }

    // 일정 수정
    public void updateEvent(String accessToken, String eventId, String title, LocalDateTime startAt, LocalDateTime endAt, String recurrenceOption) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("summary", title);

        String startStr = startAt.atZone(Z_SEOUL).format(RFC3339_OFFSET);
        String endStr = endAt.atZone(Z_SEOUL).format(RFC3339_OFFSET);

        body.put("start", Map.of("dateTime", startStr, "timeZone", Z_SEOUL.getId()));
        body.put("end", Map.of("dateTime", endStr, "timeZone", Z_SEOUL.getId()));

        if (!"NONE".equalsIgnoreCase(recurrenceOption)) {
            List<String> recurrence = switch (recurrenceOption.toUpperCase()) {
                case "DAILY" -> List.of("RRULE:FREQ=DAILY");
                case "WEEKLY" -> List.of("RRULE:FREQ=WEEKLY");
                case "BIWEEKLY" -> List.of("RRULE:FREQ=WEEKLY;INTERVAL=2");
                case "MONTHLY" -> List.of("RRULE:FREQ=MONTHLY");
                default -> null;
            };
            if (recurrence != null) {
                body.put("recurrence", recurrence);
            }
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(BASE + "/" + eventId, HttpMethod.PUT, request, Void.class);
        } catch (HttpClientErrorException.Forbidden e) {
            String msg = e.getResponseBodyAsString();
            if (msg != null && msg.contains("insufficientPermissions")) {
                throw new CustomException(ErrorCode.GOOGLE_SCOPE_INSUFFICIENT);
            }
            throw e;
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new CustomException(ErrorCode.GOOGLE_TOKEN_EXPIRED);
        } catch (HttpClientErrorException e) {
            // 구글이 보낸 400 Bad Request, 404 Not Found 등이 여기에 해당
            log.error("Google Calendar Client Error (4xx): status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.GOOGLE_API_CLIENT_ERROR);
        } catch (HttpServerErrorException e) {
            // Google 서버 자체에 문제가 있을 때 발생
            log.error("Google Calendar Server Error (5xx): status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.GOOGLE_API_SERVER_ERROR);
        } catch (ResourceAccessException e) {
            // 타임아웃 또는 네트워크 연결 자체에 문제가 있을 때 발생
            log.error("Google Calendar Network Error: {}", e.getMessage());
            throw new CustomException(ErrorCode.GOOGLE_API_NETWORK_ERROR);
        }
    }

    // 일정 삭제
    public void deleteEvent(String accessToken, String eventId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(BASE + "/" + eventId, HttpMethod.DELETE, request, Void.class);
        } catch (HttpClientErrorException.Forbidden e) {
            String msg = e.getResponseBodyAsString();
            if (msg != null && msg.contains("insufficientPermissions")) {
                throw new CustomException(ErrorCode.GOOGLE_SCOPE_INSUFFICIENT);
            }
            throw e;
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new CustomException(ErrorCode.GOOGLE_TOKEN_EXPIRED);
        }
    }

    // 단일 상세 일정 조회
    public ScheduleDetailResponse getEventById(String accessToken, String eventId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(BASE + "/" + eventId, HttpMethod.GET, request, Map.class);
            Map<String, Object> body = response.getBody();

            Map<String, String> start = (Map<String, String>) body.get("start");
            Map<String, String> end = (Map<String, String>) body.get("end");

            List<String> recurrenceList = (List<String>) body.get("recurrence");
            String recurrence = RecurrenceUtils.parseRecurrenceToLabel(recurrenceList);

            return new ScheduleDetailResponse(
                    (String) body.get("id"),
                    (String) body.getOrDefault("summary", ""),
                    parseGoogleDateTime(start),
                    parseGoogleDateTime(end),
                    recurrence
            );
        } catch (HttpClientErrorException.Forbidden e) {
            String msg = e.getResponseBodyAsString();
            if (msg != null && msg.contains("insufficientPermissions")) {
                throw new CustomException(ErrorCode.GOOGLE_SCOPE_INSUFFICIENT);
            }
            throw e;
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new CustomException(ErrorCode.GOOGLE_TOKEN_EXPIRED);
        }
    }

    // === 내부 유틸 ===

    private ScheduleResponse toScheduleResponse(Map<String, Object> item) {
        Map<String, String> start = (Map<String, String>) item.get("start");
        Map<String, String> end = (Map<String, String>) item.get("end");

        String title = (String) item.getOrDefault("summary", "제목 없음");
        String eventId = (String) item.get("id");

        List<String> recurrenceList = (List<String>) item.get("recurrence");
        String recurrence = RecurrenceUtils.parseRecurrenceToLabel(recurrenceList);

        return new ScheduleResponse(
                eventId,
                title,
                parseGoogleDateTime(start),
                parseGoogleDateTime(end),
                recurrence
        );
    }

    /**
     * Google 응답의 start/end: { "dateTime": "...+09:00", "timeZone": "..."} 또는 { "date": "2025-08-01" }
     */
    private LocalDateTime parseGoogleDateTime(Map<String, String> dt) {
        if (dt == null) return null;
        String dateTime = dt.get("dateTime");
        if (dateTime != null) {
            return OffsetDateTime.parse(dateTime).toLocalDateTime();
        }
        String date = dt.get("date"); // 종일 이벤트
        if (date != null) {
            return LocalDate.parse(date).atStartOfDay();
        }
        return null;
        // 필요하면 종일 end는 구글이 다음날 00:00로 내려주니 -1초 보정 로직도 추가 가능
    }
}
