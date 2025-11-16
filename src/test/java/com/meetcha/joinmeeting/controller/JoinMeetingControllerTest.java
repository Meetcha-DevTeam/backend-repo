package com.meetcha.joinmeeting.controller;

import com.meetcha.AcceptanceTest;
import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.util.TestDataFactory;
import com.meetcha.joinmeeting.dto.ValidateMeetingCodeResponse;
import com.meetcha.meeting.domain.MeetingEntity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.meetcha.global.exception.ErrorCode.MEETING_NOT_FOUND;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

class JoinMeetingControllerTest extends AcceptanceTest {
    @Autowired
    TestDataFactory testDataFactory;

    @Autowired
    JwtProvider jwtProvider;

    @DisplayName("미팅코드 검증에 성공한다")
    @Test
    void validateMeetingCodeShouldReturn200Code(){
        UserEntity user = testDataFactory.createUser("email1");
        MeetingEntity meeting = testDataFactory.createMeeting(user.getUserId());
        String code = meeting.getMeetingCode();
        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());

        ValidateMeetingCodeResponse response = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("meeting/code/" + code)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("data", ValidateMeetingCodeResponse.class);

        assertThat(response.getMeetingId()).isEqualTo(meeting.getMeetingId());
        assertThat(response.getIsClosed()).isFalse();
    }

    @DisplayName("존재하지 않는 미팅코드를 검증하면 404 에러를 응답한다")
    @Test
    void validateMeetingCodeShouldReturn404Code(){
        UserEntity user = testDataFactory.createUser("email1");
        String code = "unknown";
        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());

        ApiResponse response = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("meeting/code/" + code)
                .then()
                .statusCode(404)
                .extract()
                .as(ApiResponse.class);

        assertThat(response.getMessage()).isEqualTo(MEETING_NOT_FOUND.getMessage());
    }


}