package com.meetcha.reflection.controller;

import com.meetcha.auth.TestAuthHelper;
import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.global.util.DatabaseCleaner;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.project.domain.ProjectEntity;
import com.meetcha.project.domain.ProjectRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReflectionControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private TestAuthHelper testAuthHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        databaseCleaner.clear();
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = "/api/v2";
        RestAssured.port = port;
    }

    // ───────────────────────────────────────────────────────────────
    // 1) 회고 생성 API 테스트
    // ───────────────────────────────────────────────────────────────
    @DisplayName("[POST] 회고 생성 성공")
    @Test
    void createReflection_success() {
        // 사용자 + 토큰 생성
        String token = testAuthHelper.createTestUserAndGetToken();
        UserEntity user = userRepository.findByEmail("testuser@meetcha.com").orElseThrow();

        // 미팅 생성
        MeetingEntity meeting = MeetingEntity.builder()
                .meetingId(UUID.randomUUID())
                .title("테스트 미팅")
                .description("설명")
                .durationMinutes(60)
                .deadline(LocalDateTime.now().plusDays(1))
                .createdBy(user.getUserId())
                .meetingStatus(com.meetcha.meeting.domain.MeetingStatus.DONE)
                .createdAt(LocalDateTime.now())
                .meetingCode("ABC12345")
                .build();

        meetingRepository.save(meeting);

        // Body 정의
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
                .statusCode(201)
                .body("success", equalTo(true))
                .body("data.reflectionId", notNullValue());
    }

    // ───────────────────────────────────────────────────────────────
    // 2) 회고 요약 목록 조회 API 테스트
    // ───────────────────────────────────────────────────────────────
    @DisplayName("[GET] 사용자 회고 요약 목록 조회 성공")
    @Test
    void getMyReflections_success() {
        String token = testAuthHelper.createTestUserAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/reflection/list")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", anyOf(notNullValue(), hasSize(0)));
    }

    // ───────────────────────────────────────────────────────────────
    // 3) 회고 상세 조회 API 테스트
    // ───────────────────────────────────────────────────────────────
    @DisplayName("[GET] 특정 미팅 회고 상세 조회 성공")
    @Test
    void getReflectionDetail_success() {
        String token = testAuthHelper.createTestUserAndGetToken();
        UserEntity user = userRepository.findByEmail("testuser@meetcha.com").orElseThrow();

        // 미팅 생성 (DONE 상태)
        MeetingEntity meeting = MeetingEntity.builder()
                .meetingId(UUID.randomUUID())
                .title("데이터 미팅")
                .description("설명입니다")
                .durationMinutes(60)
                .deadline(LocalDateTime.now().plusDays(1))
                .createdBy(user.getUserId())
                .meetingStatus(com.meetcha.meeting.domain.MeetingStatus.DONE)
                .createdAt(LocalDateTime.now())
                .meetingCode("ABCDEFGH")
                .build();

        meetingRepository.save(meeting);

        // (테스트 간편화를 위해 회고는 직접 DB에 삽입해도 됨)
        // 하지만 여기서는 API를 이용해 생성 후 조회하는 흐름 유지

        Map<String, Object> body = Map.of(
                "contribution", 80,
                "role", "백엔드",
                "thought", "좋았음"
        );

        // 회고 생성
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/meeting/" + meeting.getMeetingId() + "/reflection/create")
                .then()
                .statusCode(201);

        // 상세 조회
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/meeting/" + meeting.getMeetingId() + "/reflection")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.meetingId", equalTo(meeting.getMeetingId().toString()))
                .body("data.contribution", equalTo(80));
    }

    // ───────────────────────────────────────────────────────────────
    // 4) 회고 통계 API 테스트
    // ───────────────────────────────────────────────────────────────
    @DisplayName("[GET] 회고 통계 조회 성공")
    @Test
    void getReflectionSummary_success() {
        String token = testAuthHelper.createTestUserAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/reflection/summary")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.totalReflections", notNullValue())
                .body("data.averageContribution", notNullValue())
                .body("data.mostFrequentRole", notNullValue());
    }
}
