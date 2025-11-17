package com.meetcha.auth.controller;

import com.meetcha.AcceptanceTest;
import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.dto.RefreshTokenRequestDto;
import com.meetcha.auth.dto.TokenResponseDto;
import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.util.TestDataFactory;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static com.meetcha.global.exception.ErrorCode.EXPIRED_REFRESH_TOKEN;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest extends AcceptanceTest {
    @Autowired
    JwtProvider jwtProvider;
    @Autowired
    private TestDataFactory testDataFactory;

    @DisplayName("토큰 재발급은 성공해야 한다")
    @Test
    void refreshShouldSucceed(){
        // given
        UserEntity user = testDataFactory.createUser("email1");
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId(), user.getEmail());

        testDataFactory.saveRefreshToken(user.getUserId(), refreshToken, LocalDateTime.now().plusHours(1));

        RefreshTokenRequestDto request = new RefreshTokenRequestDto(refreshToken);

        // when
        ValidatableResponse validatableResponse = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .when()
                .post("oauth/refresh")
                .then();

        // then
        validatableResponse.statusCode(200);
        TokenResponseDto response = validatableResponse.extract()
                .jsonPath().getObject("data", TokenResponseDto.class);
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
    }

    @DisplayName("토큰 재발급은 성공해야 한다")
    @Test
    void refreshShouldReturn401_WhenRefreshTokenExpired(){
        // given
        UserEntity user = testDataFactory.createUser("email1");
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId(), user.getEmail());

        testDataFactory.saveRefreshToken(user.getUserId(), refreshToken, LocalDateTime.now().minusHours(1));

        RefreshTokenRequestDto request = new RefreshTokenRequestDto(refreshToken);

        // when
        ValidatableResponse validatableResponse = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .when()
                .post("oauth/refresh")
                .then();

        // then
        validatableResponse.statusCode(401);
        ApiResponse response = validatableResponse.extract().as(ApiResponse.class);
        assertThat(response.getMessage()).isEqualTo(EXPIRED_REFRESH_TOKEN.getMessage());
    }
}