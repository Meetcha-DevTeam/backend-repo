package com.meetcha.auth.controller;

import com.meetcha.AcceptanceTest;
import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.auth.dto.LoginRequestDto;
import com.meetcha.auth.dto.RefreshTokenRequestDto;
import com.meetcha.auth.dto.TokenResponseDto;
import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.auth.service.LoginService;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.util.TestDataFactory;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.meetcha.global.exception.ErrorCode.EXPIRED_REFRESH_TOKEN;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

class UserControllerTest extends AcceptanceTest {
    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    private TestDataFactory testDataFactory;

    @MockitoBean
    private RestTemplate restTemplate;


    @MockitoSpyBean
    private LoginService loginService;

    @Autowired
    private UserRepository userRepository;

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

    @DisplayName("구글 로그인에 성공하면 토큰이 반환된다")
    @Test
    void googleLoginShouldReturnAccessAndRefreshToken() throws IOException {
        // given
        mockGoogleOAuthServer("email@email.com");

        LoginRequestDto request = new LoginRequestDto("google auth code", "redirect uri");

        // when
        ValidatableResponse validatableResponse = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .when()
                .post("oauth/google")
                .then();

        // then
        validatableResponse.statusCode(200);
        TokenResponseDto response = validatableResponse.extract()
                .jsonPath().getObject("data", TokenResponseDto.class);
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
    }

    private void mockGoogleOAuthServer(String email) {
        Map<String, Object> tokens = new HashMap<>();
        tokens.put("access_token", "google access token");
        tokens.put("refresh_token", "google refresh token");
        ResponseEntity<Map> tokenResponse = new ResponseEntity<>(tokens, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                Mockito.anyString(),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class),
                Mockito.eq(Map.class)
        )).thenReturn(tokenResponse);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email", email);
        userInfo.put("name", "name");
        userInfo.put("picture", "picture.png");
        ResponseEntity<Map> userInfoResponse = new ResponseEntity<>(userInfo, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                Mockito.anyString(),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(HttpEntity.class),
                Mockito.eq(Map.class)
        )).thenReturn(userInfoResponse);
    }

    @DisplayName("신규 사용자가 구글 로그인을 수행하면 계정이 생성된다")
    @Test
    void googleLoginShouldCreateUser_WhenNewComer() throws IOException {
        // given
        String email = "email@email.com";

        mockGoogleOAuthServer(email);

        LoginRequestDto request = new LoginRequestDto("google auth code", "redirect uri");

        // when
        ValidatableResponse validatableResponse = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .when()
                .post("oauth/google")
                .then();

        // then
        validatableResponse.statusCode(200);

        Optional<UserEntity> userByEmail = userRepository.findByEmail(email);
        assertThat(userByEmail).isPresent();
        assertThat(userByEmail.get().getProfileImgSrc()).isEqualTo("picture.png");
    }

    @DisplayName("기존 사용자가 구글 로그인을 수행하면 유저 정보가 갱신된다")
    @Test
    void googleLoginShouldUpdateUser_WhenExistingUser() throws IOException {
        // given
        String email = "email@email.com";
        testDataFactory.createUser(email);

        mockGoogleOAuthServer(email);

        LoginRequestDto request = new LoginRequestDto("google auth code", "redirect uri");

        // when
        ValidatableResponse validatableResponse = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .when()
                .post("oauth/google")
                .then();

        // then
        validatableResponse.statusCode(200);

        Optional<UserEntity> userByEmail = userRepository.findByEmail(email);
        assertThat(userByEmail).isPresent();
        assertThat(userByEmail.get().getProfileImgSrc()).isEqualTo("picture.png");
        assertThat(userByEmail.get().getGoogleToken()).isEqualTo("google access token");
        assertThat(userByEmail.get().getGoogleRefreshToken()).isEqualTo("google refresh token");
    }
}