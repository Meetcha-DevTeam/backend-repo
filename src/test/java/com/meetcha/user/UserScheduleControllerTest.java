package com.meetcha.user;

import com.meetcha.AcceptanceTest;
import com.meetcha.auth.TestAuthHelper;
import com.meetcha.auth.service.GoogleTokenService;
import com.meetcha.external.google.GoogleCalendarClient;
import com.meetcha.user.dto.CreateScheduleRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
}