package com.meetcha.user;

import com.meetcha.AcceptanceTest;
import com.meetcha.auth.TestAuthHelper;
import com.meetcha.auth.service.GoogleTokenService;
import com.meetcha.external.google.GoogleCalendarClient;
import com.meetcha.user.dto.CreateScheduleRequest;
import com.meetcha.user.dto.ScheduleDetailResponse;
import com.meetcha.user.dto.ScheduleResponse;
import com.meetcha.user.dto.UpdateScheduleRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("UserSchedule API + Recurrence 통합 테스트")
class UserScheduleControllerTest extends AcceptanceTest {

    @Autowired
    private TestAuthHelper testAuthHelper;

    @MockBean
    private GoogleCalendarClient googleCalendarClient;

    @MockBean
    private GoogleTokenService googleTokenService;

    // =========================
    // 1️⃣ 일정 생성 (recurrence 포함)
    // =========================
    @Test
    @DisplayName("일정 생성 시 recurrence 값이 포함되어 생성된다")
    void createSchedule_WithRecurrence() {
        String accessToken = testAuthHelper.createTestUserAndGetToken();

        when(googleTokenService.ensureValidAccessToken(any(UUID.class)))
                .thenReturn("mock-google-token");

        when(googleCalendarClient.createEvent(
                anyString(), anyString(), any(), any(), any()
        )).thenReturn("evt_create_recur");

        CreateScheduleRequest request = CreateScheduleRequest.builder()
                .title("매일 회의")
                .startAt(LocalDateTime.of(2025, 11, 10, 9, 0))
                .endAt(LocalDateTime.of(2025, 11, 10, 9, 30))
                .recurrence("DAILY")
                .build();

        given()
                .auth().oauth2(accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/user/schedule/create")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("data.eventId", equalTo("evt_create_recur"));
    }

    // =========================
    // 2️⃣ 일정 조회 (recurrence 그대로)
    // =========================
    @Test
    @DisplayName("일정 조회 시 recurrence 값이 정확히 내려온다")
    void getSchedule_WithRecurrence() {
        String accessToken = testAuthHelper.createTestUserAndGetToken();

        LocalDateTime from = LocalDateTime.of(2025, 11, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2025, 11, 30, 23, 59);

        ScheduleResponse schedule = ScheduleResponse.builder()
                .eventId("evt_weekly")
                .title("주간 회의")
                .startAt(from.plusDays(7))
                .endAt(from.plusDays(7).plusHours(1))
                .recurrence("WEEKLY")
                .build();

        when(googleTokenService.ensureValidAccessToken(any(UUID.class)))
                .thenReturn("mock-google-token");

        when(googleCalendarClient.getEvents(anyString(), any(), any()))
                .thenReturn(List.of(schedule));

        given()
                .auth().oauth2(accessToken)
                .queryParam("from", from.toString())
                .queryParam("to", to.toString())
                .when()
                .get("/user/schedule")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data[0].recurrence", equalTo("WEEKLY"));
    }

    // =========================
    // 3️⃣ recurrence 추론 로직 테스트 (DAILY)
    // =========================
    @Test
    @DisplayName("연속된 일정이 3회 이상 존재하면 DAILY로 추론된다")
    void getSchedule_RecurrenceInference_Daily() {
        String accessToken = testAuthHelper.createTestUserAndGetToken();

        LocalDateTime base = LocalDateTime.of(2025, 11, 10, 9, 0);

        List<ScheduleResponse> rawEvents = List.of(
                ScheduleResponse.builder()
                        .eventId("e1")
                        .title("아침 회의")
                        .startAt(base)
                        .endAt(base.plusMinutes(30))
                        .build(),
                ScheduleResponse.builder()
                        .eventId("e2")
                        .title("아침 회의")
                        .startAt(base.plusDays(1))
                        .endAt(base.plusDays(1).plusMinutes(30))
                        .build(),
                ScheduleResponse.builder()
                        .eventId("e3")
                        .title("아침 회의")
                        .startAt(base.plusDays(2))
                        .endAt(base.plusDays(2).plusMinutes(30))
                        .build()
        );

        when(googleTokenService.ensureValidAccessToken(any(UUID.class)))
                .thenReturn("mock-google-token");

        when(googleCalendarClient.getEvents(anyString(), any(), any()))
                .thenReturn(rawEvents);

        given()
                .auth().oauth2(accessToken)
                .queryParam("from", base.minusDays(1).toString())
                .queryParam("to", base.plusDays(3).toString())
                .when()
                .get("/user/schedule")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data[0].recurrence", equalTo("DAILY"));
    }

    // =========================
    // 4️⃣ 일정 수정 (recurrence 반영)
    // =========================
    @Test
    @DisplayName("일정 수정 시 사용자가 보낸 recurrence 값이 반영된다")
    void updateSchedule_WithRecurrence() {
        String accessToken = testAuthHelper.createTestUserAndGetToken();

        when(googleTokenService.ensureValidAccessToken(any(UUID.class)))
                .thenReturn("mock-google-token");

        doNothing().when(googleCalendarClient).updateEvent(
                anyString(), anyString(), anyString(), any(), any(), any()
        );

        UpdateScheduleRequest request = UpdateScheduleRequest.builder()
                .eventId("evt_update")
                .title("월간 회의")
                .startAt(LocalDateTime.of(2025, 12, 1, 10, 0))
                .endAt(LocalDateTime.of(2025, 12, 1, 11, 0))
                .recurrence("MONTHLY")
                .build();

        given()
                .auth().oauth2(accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/user/schedule/update")
                .then()
                .statusCode(HttpStatus.OK.value());

        verify(googleCalendarClient).updateEvent(
                eq("mock-google-token"),
                eq("evt_update"),
                eq("월간 회의"),
                any(),
                any(),
                eq("RRULE:FREQ=MONTHLY")
        );
    }

    // =========================
    // 5️⃣ 일정 삭제
    // =========================
    @Test
    @DisplayName("recurrence 여부와 상관없이 일정 삭제가 가능하다")
    void deleteSchedule_WithRecurrence() {
        String accessToken = testAuthHelper.createTestUserAndGetToken();

        when(googleTokenService.ensureValidAccessToken(any(UUID.class)))
                .thenReturn("mock-google-token");

        doNothing().when(googleCalendarClient).deleteEvent(anyString(), anyString());

        given()
                .auth().oauth2(accessToken)
                .queryParam("eventId", "evt_delete")
                .when()
                .delete("/user/schedule/delete")
                .then()
                .statusCode(HttpStatus.OK.value());

        verify(googleCalendarClient).deleteEvent(
                eq("mock-google-token"),
                eq("evt_delete")
        );
    }
}
