package com.meetcha.reflection.controller;

import com.meetcha.auth.TestAuthHelper;
import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.global.util.DatabaseCleaner;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.meeting.domain.MeetingStatus;
import com.meetcha.meeting.scheduler.MeetingStatusUpdateScheduler;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.task.scheduling.enabled=false"
)
@ActiveProfiles("test")
class ReflectionControllerTest {

    @LocalServerPort
    int port;

    @Autowired DatabaseCleaner databaseCleaner;
    @Autowired TestAuthHelper testAuthHelper;
    @Autowired UserRepository userRepository;
    @Autowired MeetingRepository meetingRepository;

    @MockBean // 테스트에서 스케줄러 비활성화
    MeetingStatusUpdateScheduler meetingStatusUpdateScheduler;

    private UserEntity user;
    private String token;

    @BeforeEach
    void setUp() {
        databaseCleaner.clear();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        token = testAuthHelper.createTestUserAndGetToken();
        user = userRepository.findByEmail("testuser@meetcha.com").orElseThrow();
    }

    /** 공통 Meeting 저장 함수 */
    private MeetingEntity persistMeeting(MeetingEntity meeting) {
        var saved = meetingRepository.saveAndFlush(meeting);
        return meetingRepository.findById(saved.getMeetingId()).orElseThrow();
    }

    // -------------------------------------------------------
    // 회고 생성
    // -------------------------------------------------------
    @DisplayName("[POST] 회고 생성 성공")
    @Test
    void createReflection_success() {

        MeetingEntity meeting = persistMeeting(
                MeetingEntity.builder()
                        .title("테스트 미팅")
                        .description("설명")
                        .durationMinutes(60)
                        .deadline(LocalDateTime.now().plusDays(1))
                        .createdBy(user.getUserId())
                        .meetingStatus(MeetingStatus.DONE)
                        .createdAt(LocalDateTime.now())
                        .meetingCode("ABC12345")
                        .build()
        );

        Map<String, Object> body = Map.of(
                "contribution", 75,
                "role", "백엔드",
                "thought", "좋았음",
                "completedWork", "API 구현",
                "plannedWork", "리팩토링"
        );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/meeting/" + meeting.getMeetingId() + "/reflection/create")
                .then()
                .statusCode(200)
                .body("data.reflectionId", notNullValue());
    }

    // -------------------------------------------------------
    // 회고 목록 요약
    // -------------------------------------------------------
    @DisplayName("[GET] 사용자 회고 요약 목록 조회 성공")
    @Test
    void getMyReflections_success() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/meeting/reflections")
                .then()
                .statusCode(200)
                .body("data", notNullValue());
    }

    // -------------------------------------------------------
    // 특정 회고 상세 조회
    // -------------------------------------------------------
    @DisplayName("[GET] 특정 미팅 회고 상세 조회 성공")
    @Test
    void getReflectionDetail_success() {

        MeetingEntity meeting = persistMeeting(
                MeetingEntity.builder()
                        .title("데이터 미팅")
                        .description("설명입니다")
                        .durationMinutes(60)
                        .deadline(LocalDateTime.now().plusDays(1))
                        .createdBy(user.getUserId())
                        .meetingStatus(MeetingStatus.DONE)
                        .createdAt(LocalDateTime.now())
                        .confirmedTime(LocalDateTime.now())
                        .meetingCode("ABCDEFGH")
                        .build()
        );

        Map<String, Object> body = Map.of(
                "contribution", 80,
                "role", "백엔드",
                "thought", "좋았음"
        );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/meeting/" + meeting.getMeetingId() + "/reflection/create")
                .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/meeting/" + meeting.getMeetingId() + "/reflection")
                .then()
                .statusCode(200)
                .body("data.meetingId", equalTo(meeting.getMeetingId().toString()))
                .body("data.contribution", equalTo(80));
    }

    // -------------------------------------------------------
    // 회고 Summary 조회
    // -------------------------------------------------------
    @DisplayName("[GET] 회고 통계 조회 성공")
    @Test
    void getReflectionSummary_success() {
        MeetingEntity m1 = persistMeeting(
                MeetingEntity.builder()
                        .title("미팅1")
                        .description("설명1")
                        .durationMinutes(30)
                        .deadline(LocalDateTime.now().plusDays(1))
                        .createdBy(user.getUserId())
                        .meetingStatus(MeetingStatus.DONE)
                        .createdAt(LocalDateTime.now())
                        .meetingCode("MMM001")
                        .build()
        );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "contribution", 70,
                        "role", "백엔드",
                        "thought", "좋았음"
                ))
                .when()
                .post("/meeting/" + m1.getMeetingId() + "/reflection/create")
                .then()
                .statusCode(200);

        MeetingEntity m2 = persistMeeting(
                MeetingEntity.builder()
                        .title("미팅2")
                        .description("설명2")
                        .durationMinutes(30)
                        .deadline(LocalDateTime.now().plusDays(1))
                        .createdBy(user.getUserId())
                        .meetingStatus(MeetingStatus.DONE)
                        .createdAt(LocalDateTime.now())
                        .meetingCode("MMM002")
                        .build()
        );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "contribution", 90,
                        "role", "프론트엔드",
                        "thought", "좋았음"
                ))
                .when()
                .post("/meeting/" + m2.getMeetingId() + "/reflection/create")
                .then()
                .statusCode(200);

        MeetingEntity m3 = persistMeeting(
                MeetingEntity.builder()
                        .title("미팅3")
                        .description("설명3")
                        .durationMinutes(30)
                        .deadline(LocalDateTime.now().plusDays(1))
                        .createdBy(user.getUserId())
                        .meetingStatus(MeetingStatus.DONE)
                        .createdAt(LocalDateTime.now())
                        .meetingCode("MMM003")
                        .build()
        );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "contribution", 50,
                        "role", "백엔드",
                        "thought", "보통"
                ))
                .when()
                .post("/meeting/" + m3.getMeetingId() + "/reflection/create")
                .then()
                .statusCode(200);


        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/reflection/summary")
                .then()
                .statusCode(200)
                .body("data.totalReflections", equalTo(3))
                .body("data.averageContribution", equalTo(70))  // (70 + 90 + 50) / 3
                .body("data.mostFrequentRole", equalTo("백엔드")); // 백엔드 2회 → 최다
    }

    @DisplayName("[GET] 회고 0개일 때 summary 조회 성공")
    @Test
    void getReflectionSummary_empty_success() {

        // 회고 0개 상태에서 호출

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/reflection/summary")
                .then()
                .statusCode(200)
                .body("data.totalReflections", equalTo(0))
                .body("data.averageContribution", equalTo(0))
                .body("data.mostFrequentRole", nullValue());
    }

}
