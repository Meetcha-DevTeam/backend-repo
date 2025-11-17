package com.meetcha.user;

import com.meetcha.AcceptanceTest;
import com.meetcha.auth.TestAuthHelper;
import com.meetcha.auth.service.GoogleTokenService;
import com.meetcha.external.google.GoogleCalendarClient;
import com.meetcha.user.dto.CreateScheduleRequest;
import com.meetcha.user.dto.ScheduleResponse;
import com.meetcha.user.dto.UpdateScheduleRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserScheduleControllerTest extends AcceptanceTest {

    @Autowired
    private TestAuthHelper testAuthHelper;

    @MockitoBean
    private GoogleCalendarClient googleCalendarClient;

    @MockitoBean
    private GoogleTokenService googleTokenService;

    @DisplayName("인증된 사용자가 새 일정을 생성하면 201 Created와 이벤트 ID를 반환한다.")
    @Test
    void createSchedule_Success() {
        // given
        String accessToken = testAuthHelper.createTestUserAndGetToken();
        String mockGoogleEventId = "evt_mock_12345abcdef";

        when(googleTokenService.ensureValidAccessToken(any(UUID.class)))
                .thenReturn("mock-google-api-token");

        when(googleCalendarClient.createEvent(
                anyString(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class), any()
        )).thenReturn(mockGoogleEventId);

        LocalDateTime start = LocalDateTime.of(2025, 11, 20, 14, 0);
        LocalDateTime end = LocalDateTime.of(2025, 11, 20, 15, 0);
        CreateScheduleRequest requestDto = CreateScheduleRequest.builder()
                .title("팀 주간 회의")
                .startAt(start)
                .endAt(end)
                .recurrence("NONE")
                .build();

        // when
        given()
                .log().all()
                .auth().oauth2(accessToken)
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/user/schedule/create")

                // then (검증)
                .then()
                .log().all()
                .statusCode(HttpStatus.CREATED.value())

                .body("code", equalTo(201))
                .body("message", equalTo("요청에 성공하였습니다."))

                .body("data.code", equalTo(201))
                .body("data.message", equalTo("CREATED"))

                .body("data.data.eventId", equalTo(mockGoogleEventId));
    }

    @DisplayName("인증된 사용자가 일정 조회를 요청하면 200 OK와 일정 목록을 반환한다.(GET /user/schedule)")
    @Test
    void getSchedule_Success() {
        // given
        String accessToken = testAuthHelper.createTestUserAndGetToken();
        LocalDateTime from = LocalDateTime.of(2025, 11, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2025, 11, 30, 23, 59);

        // Mock 객체 생성
        ScheduleResponse mockSchedule = ScheduleResponse.builder()
                .eventId("evt_schedule_001")
                .title("구글 캘린더 일정")
                .startAt(from.plusDays(10))
                .endAt(from.plusDays(10).plusHours(1))
                .recurrence("NONE")
                .build();

        List<ScheduleResponse> mockResponseList = List.of(mockSchedule);

        // Mockito 설정
        when(googleTokenService.ensureValidAccessToken(any(UUID.class)))
                .thenReturn("mock-google-api-token");

        when(googleCalendarClient.getEvents(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockResponseList);

        // when
        given()
                .log().all()
                .auth().oauth2(accessToken)
                .contentType(ContentType.JSON)
                .queryParam("from", from.toString()) // 쿼리 파라미터로 전달
                .queryParam("to", to.toString())
                .when()
                .get("/user/schedule")

                // then (검증)
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("code", equalTo(200))
                .body("message", equalTo("요청에 성공하였습니다."))
                .body("data", hasSize(1)) // 리스트의 크기가 1인지 확인
                .body("data[0].eventId", equalTo("evt_schedule_001"))
                .body("data[0].title", equalTo("구글 캘린더 일정"));
    }

    @DisplayName("인증된 사용자가 일정 수정을 요청하면 200 OK를 반환한다.(PUT /user/schedule/update)")
    @Test
    void updateSchedule_Success() {
        // given
        String accessToken = testAuthHelper.createTestUserAndGetToken();
        String eventIdToUpdate = "evt_to_update_123";

        LocalDateTime newStart = LocalDateTime.of(2025, 12, 25, 10, 0);
        LocalDateTime newEnd = LocalDateTime.of(2025, 12, 25, 11, 0);
        UpdateScheduleRequest requestDto = UpdateScheduleRequest.builder()
                .eventId(eventIdToUpdate)
                .title("수정된 일정 (X-mas)")
                .startAt(newStart)
                .endAt(newEnd)
                .recurrence("NONE")
                .build();

        when(googleTokenService.ensureValidAccessToken(any(UUID.class)))
                .thenReturn("mock-google-api-token");

        doNothing().when(googleCalendarClient).updateEvent(
                anyString(),
                anyString(),
                anyString(),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyString()
        );

        // when
        given()
                .log().all()
                .auth().oauth2(accessToken)
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .put("/user/schedule/update")

                // then (검증)
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("code", equalTo(200))
                .body("message", equalTo("요청에 성공하였습니다."));

        // googleCalendarClient.updateEvent가 요청 DTO의 정확한 인자들로 1번 호출되었는지 확인
        verify(googleCalendarClient, times(1)).updateEvent(
                eq("mock-google-api-token"),
                eq(eventIdToUpdate),
                eq(requestDto.getTitle()),
                eq(newStart),
                eq(newEnd),
                eq(requestDto.getRecurrence())
        );
    }
}