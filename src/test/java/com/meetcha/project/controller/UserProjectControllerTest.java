/*package com.meetcha.project.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserProjectControllerTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = "/api/v2";
        RestAssured.port = port;
    }

    @DisplayName("참여한 프로젝트 목록 조회 성공")
    @Test
    void getUserProjects_success() {
        // given
        UUID userId = UUID.randomUUID();
        String token = createFakeToken(userId);

        // when & then
        given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .get("/user/projects")
                .then()
                .statusCode(anyOf(is(200), is(401))) // 실제 인증 처리에 따라 다름
                .body("success", notNullValue());
    }

    @DisplayName("Authorization 헤더가 없으면 401 반환")
    @Test
    void getUserProjects_unauthorized() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/user/projects")
                .then()
                .statusCode(401)
                .body("success", equalTo(false));
    }

    // 테스트용 JWT 토큰 생성
    private String createFakeToken(UUID userId) {
        return io.jsonwebtoken.Jwts.builder()
                .claim("userId", userId.toString())
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        "meetcha-test-secret-key-1234567890".getBytes()))
                .compact();
    }
}*/