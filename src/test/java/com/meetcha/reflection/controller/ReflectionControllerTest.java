package com.meetcha.reflection.controller;

import com.meetcha.AcceptanceTest;
import com.meetcha.TestDataFactory;
import com.meetcha.auth.TestAuthHelper;
import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.reflection.domain.MeetingReflectionRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
class ReflectionControllerTest extends AcceptanceTest {

    @Autowired
    private TestAuthHelper testAuthHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingReflectionRepository reflectionRepository;

    private UserEntity getUser() {
        return userRepository.findByEmail("testuser@meetcha.com").orElseThrow();
    }

    /**
     * Meeting 저장 후 즉시 조회까지 보장하기 위한 헬퍼 메서드
     * (flush + findById 조합)
     */
    private MeetingEntity persistMeeting(MeetingEntity meeting) {
        meetingRepository.saveAndFlush(meeting);
        return meetingRepository.findById(meeting.getMeetingId()).orElseThrow();
    }

    // -------------------------------------------------------
    // 회고 생성
    // -------------------------------------------------------
    @DisplayName("[POST] 회고 생성 성공 + DB 저장 검증")
    @Test
    void createReflection_success() {
        String token = testAuthHelper.createTestUserAndGetToken();
        UserEntity user = getUser();

        MeetingEntity meeting = persistMeeting(
                TestDataFactory.createMeeting(user.getUserId(), "테스트 미팅", 60)
        );

        Map<String, Object> body = TestDataFactory.createReflectionRequest(
                75,
                "백엔드",
                "좋았음",
                "API 구현",
                "리팩토링"
        );

        String reflectionId =
                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body(body)
                        .when()
                        .post("/meeting/" + meeting.getMeetingId() + "/reflection/create")
                        .then()
                        .statusCode(200)
                        .body("data.reflectionId", notNullValue())
                        .extract()
                        .jsonPath()
                        .getString("data.reflectionId");

        assertThat(
                reflectionRepository.findById(UUID.fromString(reflectionId))
        ).isPresent();
    }

    // -------------------------------------------------------
    // 회고 Summary 조회
    // -------------------------------------------------------
    @DisplayName("[GET] 회고 통계 조회 성공")
    @Test
    void getReflectionSummary_success() {
        String token = testAuthHelper.createTestUserAndGetToken();
        UserEntity user = getUser();

        MeetingEntity m1 = persistMeeting(
                TestDataFactory.createMeeting(user.getUserId(), "미팅1", 30)
        );
        MeetingEntity m2 = persistMeeting(
                TestDataFactory.createMeeting(user.getUserId(), "미팅2", 30)
        );
        MeetingEntity m3 = persistMeeting(
                TestDataFactory.createMeeting(user.getUserId(), "미팅3", 30)
        );

        // ---- 회고 3개 생성 ----
        given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(TestDataFactory.createReflectionRequest(
                        70, "백엔드", "괜찮았음", "기능 구현", "개선 작업"
                ))
                .when().post("/meeting/" + m1.getMeetingId() + "/reflection/create");

        given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(TestDataFactory.createReflectionRequest(
                        90, "프론트엔드", "집중 잘됨", "UI 개발", "리팩토링"
                ))
                .when().post("/meeting/" + m2.getMeetingId() + "/reflection/create");

        given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(TestDataFactory.createReflectionRequest(
                        50, "백엔드", "아쉬움", "버그 수정", "테스트 보완"
                ))
                .when().post("/meeting/" + m3.getMeetingId() + "/reflection/create");

        // ---- Summary 검증 ----
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/reflection/summary")
                .then()
                .statusCode(200)
                .body("data.totalReflections", equalTo(3))
                .body("data.averageContribution", equalTo(70))
                .body("data.mostFrequentRole", equalTo("백엔드"));
    }
}