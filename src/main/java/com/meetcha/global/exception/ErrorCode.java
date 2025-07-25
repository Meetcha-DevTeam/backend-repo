package com.meetcha.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    //400 Bad Request
    INVALID_GOOGLE_CODE(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 구글 인가 코드입니다."),
    INVALID_MEETING_DEADLINE(HttpStatus.BAD_REQUEST, 400, "참여 마감 시간은 후보 날짜보다 이르거나 같아야 합니다."),
    INVALID_MEETING_REQUEST(HttpStatus.BAD_REQUEST, 400, "입력값이 유효하지 않습니다."),
    INVALID_DURATION(HttpStatus.BAD_REQUEST, 400, "1분 이상 719분 이하로 설정해주세요."),
    INVALID_CANDIDATE_DATES(HttpStatus.BAD_REQUEST, 400, "후보 날짜는 최소 1개 이상, 최대 10개까지 가능합니다."),
    INVALID_CANDIDATE_DATE_IN_PAST(HttpStatus.BAD_REQUEST, 400, "모든 후보 날짜는 현재 날짜 이후여야 합니다."),
    MEETING_DEADLINE_PASSED(HttpStatus.BAD_REQUEST, 400, "미팅 참여마감시간이 지났습니다."),
    ALREADY_VOTED_ALTERNATIVE(HttpStatus.BAD_REQUEST, 400, "이미 대안시간 투표를 제출하였습니다."),
    ALREADY_SUBMITTED_REFLECTION(HttpStatus.BAD_REQUEST,400, "이미 회고를 작성한 미팅입니다."),

    //401 Unauthorized
    MISSING_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, 401, "인증 토큰이 필요합니다."),
    EXPIRED_JWT(HttpStatus.UNAUTHORIZED, 401, "JWT 토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, 401, "유효하지 않은 refresh Token입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, 401, "만료된 refresh Token입니다."),
    GOOGLE_TOKEN_REQUEST_FAILED(HttpStatus.UNAUTHORIZED, 401, "구글 토큰 요청 실패"),
    GOOGLE_USERINFO_REQUEST_FAILED(HttpStatus.UNAUTHORIZED, 401, "구글 유저 정보 요청 실패"),
    MALFORMED_JWT(HttpStatus.UNAUTHORIZED, 401, "형식이 잘못된 JWT 토큰입니다."),

    //404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "사용자를 찾을 수 없습니다."),
    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "미팅을 찾을 수 없습니다."),
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "미팅 참여자를 찾을 수 없습니다."),

    //500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "알 수 없는 서버 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}