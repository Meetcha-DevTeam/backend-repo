package com.meetcha.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode implements ErrorCodeBase {

    //400 Bad Request
    INVALID_GOOGLE_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 구글 인가 코드입니다."),
    INVALID_MEETING_DEADLINE(HttpStatus.BAD_REQUEST, "참여 마감 시간은 후보 날짜보다 이르거나 같아야 합니다."),
    INVALID_MEETING_REQUEST(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),
    INVALID_DURATION(HttpStatus.BAD_REQUEST, "1분 이상 719분 이하로 설정해주세요."),
    INVALID_CANDIDATE_DATES(HttpStatus.BAD_REQUEST, "후보 날짜는 최소 1개 이상, 최대 10개까지 가능합니다."),
    INVALID_CANDIDATE_DATE_IN_PAST(HttpStatus.BAD_REQUEST, "모든 후보 날짜는 현재 날짜 이후여야 합니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "날짜 형식이 잘못되었거나 범위가 유효하지 않습니다."),
    MEETING_DEADLINE_PASSED(HttpStatus.BAD_REQUEST, "미팅 참여마감시간이 지났습니다."),

    //401 Unauthorized
    MISSING_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "인증 토큰이 필요합니다."),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    EXPIRED_JWT(HttpStatus.UNAUTHORIZED, "JWT 토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 refresh Token입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 refresh Token입니다."),
    GOOGLE_TOKEN_REQUEST_FAILED(HttpStatus.UNAUTHORIZED, "구글 토큰 요청 실패"),
    GOOGLE_USERINFO_REQUEST_FAILED(HttpStatus.UNAUTHORIZED, "구글 유저 정보 요청 실패"),
    MALFORMED_JWT(HttpStatus.UNAUTHORIZED, "형식이 잘못된 JWT 토큰입니다."),
    GOOGLE_SCOPE_INSUFFICIENT(HttpStatus.UNAUTHORIZED,"Google Calendar 권한 동의가 필요합니다."),
    GOOGLE_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,"Google access token이 만료되었습니다."),
    MISSING_GOOGLE_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED,"유저의 Google access token이 존재하지 않습니다."),
    MISSING_GOOGLE_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"유저의 Google refresh token이 존재하지 않습니다."),

    //403 FORBIDDEN
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    //404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,  "사용자를 찾을 수 없습니다."),
    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "미팅을 찾을 수 없습니다."),
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "미팅 참여자를 찾을 수 없습니다."),
    REFLECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "회고를 찾을 수 없습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."),

    //409 Conflict
    ALREADY_VOTED_ALTERNATIVE(HttpStatus.BAD_REQUEST, "이미 대안시간 투표를 제출하였습니다."),
    ALREADY_SUBMITTED_REFLECTION(HttpStatus.BAD_REQUEST, "이미 회고를 작성한 미팅입니다."),
    CANNOT_DELETE_MEETING(HttpStatus.BAD_REQUEST, "매칭 실패 상태의 미팅만 삭제할 수 있습니다."),
    DUPLICATE_PROJECT_NAME(HttpStatus.CONFLICT, "이미 존재하는 프로젝트 이름입니다."),
    ALREADY_JOINED_MEETING(HttpStatus.CONFLICT, "이미 이 미팅에 참가했습니다."),
    NO_PARTICIPANT_AVAILABILITY(HttpStatus.CONFLICT, "참여자 가용 시간이 없어 미팅을 확정할 수 없습니다."),

    //500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 서버 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getCode() {
        return httpStatus.value();
    }
}