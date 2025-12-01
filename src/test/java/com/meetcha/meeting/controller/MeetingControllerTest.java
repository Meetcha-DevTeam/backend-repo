package com.meetcha.meeting.controller;

import com.meetcha.AcceptanceTest;
import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.util.TestDataFactory;
import com.meetcha.meeting.dto.MeetingCreateRequest;
import com.meetcha.meeting.dto.MeetingCreateResponse;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.meetcha.global.exception.ErrorCode.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

class MeetingControllerTest extends AcceptanceTest {
    @Autowired
    TestDataFactory testDataFactory;

    @Autowired
    JwtProvider jwtProvider;


    @DisplayName("미팅 생성에 성공한다")
    @Test
    void createMeetingShouldReturn200Code(){
        UserEntity user = testDataFactory.createUser("email");
        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());
        MeetingCreateRequest request = getCreateMeetingRequest();

        MeetingCreateResponse response = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .when()
                .post("meeting/create")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("data", MeetingCreateResponse.class);

        assertThat(response.getMeetingId()).isNotNull();
        assertThat(response.getCreatedAt()).isNotNull();
    }

    private static MeetingCreateRequest getCreateMeetingRequest() {
        LocalDateTime now = LocalDateTime.now();

        return new MeetingCreateRequest("모임1",
                "모임1입니다",
                60,
                List.of(now.toLocalDate().plusDays(5)),
                now.plusDays(3),
                null);
    }

    @DisplayName("durationMinutes가 유효하지 않으면 400에러를 응답한다")
    @Test
    void createMeetingShouldReturn400Code_WhenInvalidDurationMinutes(){
        UserEntity user = testDataFactory.createUser("email");
        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());
        MeetingCreateRequest request = getCreateMeetingRequestWithInvalidDurationMinutes();

        ApiResponse<Map<String, String>> response =
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body(request)
                        .when()
                        .post("/meeting/create")
                        .then()
                        .statusCode(400)
                        .extract()
                        .as(new TypeRef<ApiResponse<Map<String, String>>>() {});

        assertThat(response.getCode()).isEqualTo(INVALID_MEETING_REQUEST.getCode());
    }

    private MeetingCreateRequest getCreateMeetingRequestWithInvalidDurationMinutes() {
        LocalDateTime now = LocalDateTime.now();

        return new MeetingCreateRequest("모임1",
                "모임1입니다",
                720,
                List.of(now.toLocalDate().plusDays(5)),
                now.plusDays(3),
                null);
    }

    @DisplayName("candidateDates가 유효하지 않으면 400에러를 응답한다")
    @Test
    void createMeetingShouldReturn400Code_WhenInvalidCandidateDates(){
        UserEntity user = testDataFactory.createUser("email");
        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());
        MeetingCreateRequest request = getCreateMeetingRequestWithInvalidCandidateDates();

        ApiResponse<Map<String, String>> response =
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body(request)
                        .when()
                        .post("/meeting/create")
                        .then()
                        .statusCode(400)
                        .extract()
                        .as(new TypeRef<ApiResponse<Map<String, String>>>() {});

        assertThat(response.getCode()).isEqualTo(INVALID_MEETING_REQUEST.getCode());
    }

    private MeetingCreateRequest getCreateMeetingRequestWithInvalidCandidateDates() {
        LocalDateTime now = LocalDateTime.now();

        return new MeetingCreateRequest("모임1",
                "모임1입니다",
                720,
                List.of(),
                now.plusDays(3),
                null);
    }

    @DisplayName("candidateDates에 과거가 존재하면 400에러를 응답한다")
    @Test
    void createMeetingShouldReturn400Code_WhenCandidateDatesContainPast(){
        UserEntity user = testDataFactory.createUser("email");
        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());
        MeetingCreateRequest request = getCreateMeetingRequestWithPastCandidateDate();

        ApiResponse<Map<String, String>> response =
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body(request)
                        .when()
                        .post("/meeting/create")
                        .then()
                        .statusCode(400)
                        .extract()
                        .as(new TypeRef<ApiResponse<Map<String, String>>>() {});

        assertThat(response.getCode()).isEqualTo(INVALID_MEETING_REQUEST.getCode());
    }

    private MeetingCreateRequest getCreateMeetingRequestWithPastCandidateDate() {
        LocalDateTime now = LocalDateTime.now();

        return new MeetingCreateRequest("모임1",
                "모임1입니다",
                720,
                List.of(now.toLocalDate().minusDays(1)),
                now.plusDays(3),
                null);
    }

    @DisplayName("deadline이 candidateDates보다 늦으면 400에러를 응답한다")
    @Test
    void createMeetingShouldReturn400Code_WhenDeadlineIsAfterCandidateDates(){
        UserEntity user = testDataFactory.createUser("email");
        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());
        MeetingCreateRequest request = getCreateMeetingRequestWithDeadlineAfterCandidateDates();

        ApiResponse<Map<String, String>> response =
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body(request)
                        .when()
                        .post("/meeting/create")
                        .then()
                        .statusCode(400)
                        .extract()
                        .as(new TypeRef<ApiResponse<Map<String, String>>>() {});

        assertThat(response.getCode()).isEqualTo(INVALID_MEETING_REQUEST.getCode());
    }

    private MeetingCreateRequest getCreateMeetingRequestWithDeadlineAfterCandidateDates() {
        LocalDateTime now = LocalDateTime.now();

        return new MeetingCreateRequest("모임1",
                "모임1입니다",
                720,
                List.of(now.toLocalDate().plusDays(5)),
                now.plusDays(7),
                null);
    }

}