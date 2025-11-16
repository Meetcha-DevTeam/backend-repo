package com.meetcha.joinmeeting.controller;

import com.meetcha.AcceptanceTest;
import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.util.TestDataFactory;
import com.meetcha.joinmeeting.dto.JoinMeetingRequest;
import com.meetcha.joinmeeting.dto.JoinMeetingResponse;
import com.meetcha.joinmeeting.dto.ValidateMeetingCodeResponse;
import com.meetcha.meeting.domain.MeetingCandidateDateEntity;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.dto.MeetingInfoResponse;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.meetcha.global.exception.ErrorCode.*;
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
        MeetingEntity meeting = testDataFactory.createMeeting(user.getUserId(), LocalDateTime.now(), LocalDateTime.now().plusDays(2));
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

    @DisplayName("미팅 정보 조회에 성공한다")
    @Test
    void getMeetingInfoShouldSucceed(){
        // given
        UserEntity user = testDataFactory.createUser("email1");
        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());

        MeetingEntity meeting = testDataFactory.createMeeting(user.getUserId(), LocalDateTime.now(), LocalDateTime.now().plusDays(2));

        // when
        MeetingInfoResponse response = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("meeting/id/" + meeting.getMeetingId())
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("data", MeetingInfoResponse.class);

        // then
        assertThat(response.getMeetingId()).isEqualTo(meeting.getMeetingId());
        assertThat(response.getCandidateDates()).containsExactlyInAnyOrder(meeting.getCandidateDates().get(0).getCandidateDate(), meeting.getCandidateDates().get(1).getCandidateDate());
    }

    @DisplayName("미팅이 존재하지 않으면 404에러를 응답한다")
    @Test
    void getMeetingInfoShouldReturn404Code(){
        // given
        UserEntity user = testDataFactory.createUser("email1");
        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());

        UUID unknownMeetingId = UUID.randomUUID();

        // when
        ValidatableResponse validatableResponse = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("meeting/id/" + unknownMeetingId)
                .then();


        // then
        validatableResponse.statusCode(404);

        ApiResponse response = validatableResponse.extract().as(ApiResponse.class);
        assertThat(response.getMessage()).isEqualTo(MEETING_NOT_FOUND.getMessage());
    }

    @DisplayName("미팅 참가에 성공한다")
    @Test
    void joinMeetingShouldSucceed(){
        // given
        UserEntity creator = testDataFactory.createUser("email1");
        String accessToken = jwtProvider.createAccessToken(creator.getUserId(), creator.getEmail());

        MeetingEntity meeting = testDataFactory.createMeeting(creator.getUserId(), LocalDateTime.now(), LocalDateTime.now().plusDays(2));

        JoinMeetingRequest request = getJoinMeetingRequest(meeting.getCandidateDates());

        // when
        ValidatableResponse validatableResponse = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .when()
                .post("meeting/id/" + meeting.getMeetingId() + "/join")
                .then();

        // then
        validatableResponse.statusCode(200);

        JoinMeetingResponse response = validatableResponse.extract()
                .jsonPath()
                .getObject("data", JoinMeetingResponse.class);

        assertThat(response.getMeetingId()).isNotNull();
        assertThat(response.getParticipantId()).isNotNull();
    }

    private JoinMeetingRequest getJoinMeetingRequest(List<MeetingCandidateDateEntity> candidateDateEntities) {
        LocalDate availableDate = candidateDateEntities.get(0).getCandidateDate();
        LocalDateTime start = LocalDateTime.of(availableDate.getYear(), availableDate.getMonth(), availableDate.getDayOfMonth(), 12, 0);
        LocalDateTime end = LocalDateTime.of(availableDate.getYear(), availableDate.getMonth(), availableDate.getDayOfMonth(), 14, 0);
        return new JoinMeetingRequest("방장", List.of(new JoinMeetingRequest.TimeSlot(start, end)));
    }

    @DisplayName("미팅이 존재하지 않으면 404코드를 응답한다")
    @Test
    void joinMeetingReturn404Code_WhenMeetingDoesNotExists(){
        // given
        UserEntity creator = testDataFactory.createUser("email1");
        String accessToken = jwtProvider.createAccessToken(creator.getUserId(), creator.getEmail());

        UUID unknownMeetingId = UUID.randomUUID();

        LocalDateTime availableTime = LocalDateTime.now().plusDays(2);
        JoinMeetingRequest request = getJoinMeetingRequest(availableTime, availableTime.plusHours(3));

        // when
        ValidatableResponse validatableResponse = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .when()
                .post("meeting/id/" + unknownMeetingId + "/join")
                .then();

        // then
        validatableResponse.statusCode(404);
        ApiResponse response = validatableResponse.extract().as(ApiResponse.class);
        assertThat(response.getMessage()).isEqualTo(MEETING_NOT_FOUND.getMessage());

    }

    private JoinMeetingRequest getJoinMeetingRequest(LocalDateTime... availableTimes) {
        if(availableTimes.length % 2 != 0){
            throw new IllegalArgumentException("인자가 짝수개여야 합니다");
        }
        List<JoinMeetingRequest.TimeSlot> timeSlots = new ArrayList<>();
        for(int i = 0; i < availableTimes.length; i += 2){
            LocalDateTime start = LocalDateTime.of(availableTimes[i].getYear(), availableTimes[i].getMonth(), availableTimes[i].getDayOfMonth(), availableTimes[i].getHour(), availableTimes[i].getMinute());
            LocalDateTime end = LocalDateTime.of(availableTimes[i + 1].getYear(), availableTimes[i + 1].getMonth(), availableTimes[i + 1].getDayOfMonth(), availableTimes[i + 1].getHour(), availableTimes[i + 1].getMinute());
            timeSlots.add(new JoinMeetingRequest.TimeSlot(start, end));
        }
        return new JoinMeetingRequest("방장", timeSlots);
    }

    @DisplayName("미팅이 마감되었으면 400코드를 응답한다")
    @Test
    void joinMeetingReturn400Code_WhenMeetingDeadlinePassed(){
        // given
        UserEntity creator = testDataFactory.createUser("email1");
        String accessToken = jwtProvider.createAccessToken(creator.getUserId(), creator.getEmail());

        MeetingEntity meeting = testDataFactory.createMeeting(creator.getUserId(), LocalDateTime.now().minusDays(4), LocalDateTime.now().minusDays(1));

        JoinMeetingRequest request = getJoinMeetingRequest(meeting.getCandidateDates());

        // when
        ValidatableResponse validatableResponse = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .when()
                .post("meeting/id/" + meeting.getMeetingId() + "/join")
                .then();

        // then
        validatableResponse.statusCode(400);
        ApiResponse response = validatableResponse.extract().as(ApiResponse.class);
        assertThat(response.getMessage()).isEqualTo(MEETING_DEADLINE_PASSED.getMessage());
    }

    @DisplayName("중복으로 미팅 참가 요청을 보내면 400코드를 응답한다")
    @Test
    void joinMeetingReturn400Code_WhenDuplicateJoinRequest(){
        // given
        UserEntity creator = testDataFactory.createUser("email1");
        String accessToken = jwtProvider.createAccessToken(creator.getUserId(), creator.getEmail());

        MeetingEntity meeting = testDataFactory.createMeeting(creator.getUserId(), LocalDateTime.now(), LocalDateTime.now().plusDays(2));

        testDataFactory.createMeetingParticipant("방장", creator.getUserId(), meeting.getMeetingId());

        JoinMeetingRequest request = getJoinMeetingRequest(meeting.getCandidateDates());

        // when
        ValidatableResponse validatableResponse = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .when()
                .post("meeting/id/" + meeting.getMeetingId() + "/join")
                .then();

        // then
        validatableResponse.statusCode(409);
        ApiResponse response = validatableResponse.extract().as(ApiResponse.class);
        assertThat(response.getMessage()).isEqualTo(ALREADY_JOINED_MEETING.getMessage());
    }


}