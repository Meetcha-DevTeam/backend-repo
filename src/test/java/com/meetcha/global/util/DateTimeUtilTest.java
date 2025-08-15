package com.meetcha.global.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilTest {

    @Test
    @DisplayName("UTC Z 표기 문자열 → KST LocalDateTime 변환")
    void testIsoZStringToLocalDateTime_KST() {
        // UTC 시간
        String utcZ = "2025-07-22T16:00:00Z";
        LocalDateTime kstTime = DateTimeUtil.toLocalDateTime(utcZ);

        // KST는 +9시간
        assertEquals(LocalDateTime.of(2025, 7, 23, 1, 0), kstTime);
    }

    @Test
    @DisplayName("ISO_LOCAL_DATE_TIME(T 구분) → LocalDateTime 변환")
    void testIsoStringToLocalDateTime() {
        String isoString = "2025-07-22T16:00:00";
        LocalDateTime dateTime = DateTimeUtil.toLocalDateTime(isoString);

        assertEquals(2025, dateTime.getYear());
        assertEquals(7, dateTime.getMonthValue());
        assertEquals(22, dateTime.getDayOfMonth());
        assertEquals(16, dateTime.getHour());
        assertEquals(0, dateTime.getMinute());
    }

    @Test
    @DisplayName("공백 구분 문자열 → LocalDateTime 변환")
    void testSpaceSeparatedStringToLocalDateTime() {
        String spaceString = "2025-07-22 16:00:00";
        LocalDateTime dateTime = DateTimeUtil.toLocalDateTime(spaceString);

        assertEquals(LocalDateTime.of(2025, 7, 22, 16, 0), dateTime);
    }

    @Test
    @DisplayName("LocalDateTime → 문자열 변환")
    void testLocalDateTimeToString() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 7, 22, 16, 0);
        String result = DateTimeUtil.toString(dateTime);

        assertEquals("2025-07-22 16:00:00", result);
    }

    @Test
    @DisplayName("UTC → KST 변환")
    void testUtcToKstConversion() {
        LocalDateTime utcTime = LocalDateTime.of(2025, 7, 22, 7, 0); // UTC 07:00
        LocalDateTime kstTime = DateTimeUtil.utcToKst(utcTime);

        assertEquals(LocalDateTime.of(2025, 7, 22, 16, 0), kstTime);
    }

    @Test
    @DisplayName("KST → UTC 변환")
    void testKstToUtcConversion() {
        LocalDateTime kstTime = LocalDateTime.of(2025, 7, 22, 16, 0); // KST 16:00
        LocalDateTime utcTime = DateTimeUtil.kstToUtc(kstTime);

        assertEquals(LocalDateTime.of(2025, 7, 22, 7, 0), utcTime);
    }

    @Test
    @DisplayName("잘못된 날짜 형식 예외 처리")
    void testInvalidDateFormat() {
        String invalid = "2025/07/22 16:00:00"; // 슬래시 사용 → 지원 안 함
        assertThrows(IllegalArgumentException.class, () -> DateTimeUtil.toLocalDateTime(invalid));
    }
}
