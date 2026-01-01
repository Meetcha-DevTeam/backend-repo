package com.meetcha.meetinglist.controller;

import com.meetcha.AcceptanceTest;
import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.util.TestDataFactory;
import com.meetcha.joinmeeting.domain.MeetingParticipant;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meetinglist.dto.MeetingAllAvailabilitiesResponse;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

class MeetingListControllerTest extends AcceptanceTest {

    @Autowired TestDataFactory testDataFactory;
    @Autowired JwtProvider jwtProvider;

    @DisplayName("모든 미팅 참여자의 모든 참가 가능 시간을 조회한다")
    @Test
    void getAllAvailabilities() {
        // given
        UserEntity user = testDataFactory.createUser("email1");
        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());

        MeetingEntity meeting = testDataFactory.createMeeting(user.getUserId(), LocalDateTime.now(), LocalDateTime.now().plusDays(3));

        // 2. 미팅 참가자 생성
        MeetingParticipant participant =
                testDataFactory.createMeetingParticipant(
                        "참가자1",
                        user.getUserId(),
                        meeting.getMeetingId()
                );

        // 3. 참가 가능 시간 생성
        testDataFactory.createParticipantAvailability(
                participant.getParticipantId(),
                meeting.getMeetingId(),
                LocalDateTime.of(2025, 7, 22, 15, 0),
                LocalDateTime.of(2025, 7, 22, 15, 30)
        );


        // when
        MeetingAllAvailabilitiesResponse response =
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .when()
                        .get("/meeting-lists/{meetingId}/availabilities",
                                meeting.getMeetingId())
                        .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getObject("data", MeetingAllAvailabilitiesResponse.class);

        // then
        assertThat(response.getCount()).isEqualTo(1);
        assertThat(response.getParticipants()).hasSize(1);
        assertThat(response.getParticipants().get(0).getParticipantId())
                .isEqualTo(participant.getParticipantId());
        assertThat(response.getParticipants().get(0).getAvailabilities())
                .hasSize(1);
    }

}
