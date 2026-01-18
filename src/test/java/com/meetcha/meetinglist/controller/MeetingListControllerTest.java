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
import java.util.UUID;

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

    @DisplayName("참여자가 여러 명일 때, 미제출 참여자도 빈 availabilities로 포함된다")
    @Test
    void getAllAvailabilities_includeParticipantsWithoutAvailability() {
        // given
        UserEntity user = testDataFactory.createUser("email2");
        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());

        MeetingEntity meeting =
                testDataFactory.createMeeting(
                        user.getUserId(),
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(3)
                );

        // 참가자 3명
        MeetingParticipant p1 =
                testDataFactory.createMeetingParticipant("p1", user.getUserId(), meeting.getMeetingId());

        MeetingParticipant p2 =
                testDataFactory.createMeetingParticipant("p2", UUID.randomUUID(), meeting.getMeetingId());

        MeetingParticipant p3 =
                testDataFactory.createMeetingParticipant("p3", UUID.randomUUID(), meeting.getMeetingId());

        // p1: 2개
        LocalDateTime base = LocalDateTime.now().plusHours(1).withSecond(0).withNano(0);

        testDataFactory.createParticipantAvailability(
                p1.getParticipantId(),
                meeting.getMeetingId(),
                base,
                base.plusMinutes(30)
        );

        testDataFactory.createParticipantAvailability(
                p1.getParticipantId(),
                meeting.getMeetingId(),
                base.plusHours(1),
                base.plusHours(1).plusMinutes(30)
        );

        // p2: 1개
        testDataFactory.createParticipantAvailability(
                p2.getParticipantId(),
                meeting.getMeetingId(),
                base.plusHours(2),
                base.plusHours(3)
        );

        // p3: 제출 안 함 (0개)

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
        assertThat(response.getCount()).isEqualTo(3);
        assertThat(response.getParticipants()).hasSize(3);

        var map = response.getParticipants().stream()
                .collect(java.util.stream.Collectors.toMap(
                        MeetingAllAvailabilitiesResponse.ParticipantAvailabilities::getParticipantId,
                        p -> p.getAvailabilities().size()
                ));

        assertThat(map.get(p1.getParticipantId())).isEqualTo(2);
        assertThat(map.get(p2.getParticipantId())).isEqualTo(1);
        assertThat(map.get(p3.getParticipantId())).isEqualTo(0);

    }


}