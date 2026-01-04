package com.meetcha.project.controller;

import com.meetcha.AcceptanceTest;
import com.meetcha.auth.TestAuthHelper;
import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.project.domain.ProjectEntity;
import com.meetcha.project.domain.ProjectRepository;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

class UserProjectControllerTest extends AcceptanceTest {

    @Autowired
    private TestAuthHelper testAuthHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    // --------------------------------------------------
    // 1) 프로젝트 생성 성공 + DB 검증
    // --------------------------------------------------
    @DisplayName("[POST] 프로젝트 생성 성공 + DB 저장 검증")
    @Test
    void createProject_success_and_verify_db() {
        // given
        String token = testAuthHelper.createTestUserAndGetToken();
        Map<String, Object> body = Map.of("name", "밋챠 백엔드");

        // when
        ExtractableResponse<Response> response =
                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body(body)
                        .when()
                        .post("/user/projects")
                        .then()
                        .statusCode(200)
                        .extract();

        String projectId = response.jsonPath().getString("data.projectId");
        String projectName = response.jsonPath().getString("data.name");

        // then - DB 검증
        ProjectEntity project = projectRepository.findById(UUID.fromString(projectId))
                .orElseThrow(() -> new IllegalStateException("프로젝트 DB 저장 실패"));

        assertThat(project.getName()).isEqualTo(projectName);
    }

    // --------------------------------------------------
    // 2) 프로젝트 생성 실패 - 중복 이름
    // --------------------------------------------------
    @DisplayName("[POST] 프로젝트 생성 실패 - 중복된 프로젝트 이름")
    @Test
    void createProject_fail_duplicate_name() {
        // given
        String token = testAuthHelper.createTestUserAndGetToken();

        UserEntity user = userRepository.findByEmail("testuser@meetcha.com")
                .orElseThrow();

        projectRepository.save(
                ProjectEntity.builder()
                        .projectId(UUID.randomUUID())
                        .user(user)
                        .name("밋챠 백엔드")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        Map<String, Object> body = Map.of("name", "밋챠 백엔드");

        // when & then
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/user/projects")
                .then()
                .statusCode(409);
    }

    // --------------------------------------------------
    // 3) 프로젝트 목록 조회 성공 + DB 값 비교
    // --------------------------------------------------
    @DisplayName("[GET] 참여한 프로젝트 목록 조회 성공 + DB 값 비교")
    @Test
    void getUserProjects_success_verify_db() {

        String token = testAuthHelper.createTestUserAndGetToken();

        UserEntity user = userRepository.findByEmail("testuser@meetcha.com")
                .orElseThrow();

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

        // when
        ExtractableResponse<Response> response =
                given()
                        .header("Authorization", "Bearer " + token)
                        .accept(ContentType.JSON)
                        .when()
                        .get("/user/projects")
                        .then()
                        .statusCode(200)
                        .extract();

        List<String> projectNames = response.jsonPath().getList("data.projectName");

        // then - DB 값과 비교
        List<String> dbProjectNames = projectRepository.findAllByUser_UserId(user.getUserId())
                .stream()
                .map(ProjectEntity::getName)
                .toList();

        assertThat(projectNames).containsExactlyInAnyOrderElementsOf(dbProjectNames);
    }

    // --------------------------------------------------
    // 4) 참여 프로젝트 없을 때
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
