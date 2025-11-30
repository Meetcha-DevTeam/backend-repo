package com.meetcha.project.controller;

import com.meetcha.auth.TestAuthHelper;
import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.global.util.DatabaseCleaner;
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
class UserProjectControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private TestAuthHelper testAuthHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        databaseCleaner.clear();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    // --------------------------------------------------
    // 1) 프로젝트 생성 테스트
    // --------------------------------------------------
    @DisplayName("[POST] 프로젝트 생성 성공")
    @Test
    void createProject_success() {
        // given
        String token = testAuthHelper.createTestUserAndGetToken();

        Map<String, Object> body = Map.of("name", "밋챠 백엔드");

        // when & then
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/user/projects")
                .then()
                .statusCode(200)
                .body("data.projectId", notNullValue())
                .body("data.name", equalTo("밋챠 백엔드"))
                .body("data.createdAt", notNullValue());
    }

    // --------------------------------------------------
    // 2) 프로젝트 목록 조회 성공
    // --------------------------------------------------
    @DisplayName("[GET] 참여한 프로젝트 목록 조회 성공")
    @Test
    void getUserProjects_success() {

        // 1) 테스트 유저 + JWT
        String token = testAuthHelper.createTestUserAndGetToken();

        // 2) DB에서 테스트 유저 조회
        UserEntity user = userRepository.findByEmail("testuser@meetcha.com")
                .orElseThrow(() -> new IllegalStateException("테스트 유저 조회 실패"));

        // 3) 유저가 참여한 프로젝트 2개 생성
        projectRepository.save(
                ProjectEntity.builder()
                        .projectId(UUID.randomUUID())
                        .user(user)
                        .name("밋챠 백엔드")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        projectRepository.save(
                ProjectEntity.builder()
                        .projectId(UUID.randomUUID())
                        .user(user)
                        .name("졸프 프론트")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // 4) 조회 API 호출 + 검증
        given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .get("/user/projects")
                .then()
                .statusCode(200)
                .body("data", hasSize(2))
                .body("data[0].projectName", notNullValue());
    }

    // --------------------------------------------------
    // 3) 참여 프로젝트가 없을 때
    // --------------------------------------------------
    @DisplayName("[GET] 참여 프로젝트 없으면 빈 배열 반환")
    @Test
    void getUserProjects_empty() {
        String token = testAuthHelper.createTestUserAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .get("/user/projects")
                .then()
                .statusCode(200)
                .body("data", hasSize(0));
    }
}
