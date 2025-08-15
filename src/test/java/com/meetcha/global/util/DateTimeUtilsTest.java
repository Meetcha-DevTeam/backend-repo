package com.meetcha.global.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilsTest {

    @Test
    @DisplayName("KST 문자열 → UTC LocalDateTime 변환")
    void testKstStringToUtc() {
        // given
        String kstStr = "2025-05-26T09:00:00"; // KST 기준

        // when
        LocalDateTime utcDateTime = DateTimeUtils.kstStringToUtc(kstStr);

        // then
        assertEquals(LocalDateTime.of(2025, 5, 26, 0, 0, 0), utcDateTime);
    }

    @Test
    @DisplayName("UTC LocalDateTime → KST 문자열 변환")
    void testUtcToKstString() {
        // given
        LocalDateTime utcDateTime = LocalDateTime.of(2025, 5, 26, 0, 0, 0); // UTC 기준

        // when
        String kstStr = DateTimeUtils.utcToKstString(utcDateTime);

        // then
        assertEquals("2025-05-26T09:00:00", kstStr);
    }

    @Test
    @DisplayName("KST ↔ UTC 변환 상호 검증")
    void testKstUtcRoundTrip() {
        // given
        LocalDateTime originalKst = LocalDateTime.of(2025, 5, 26, 15, 30, 0);

        // when
        LocalDateTime utc = DateTimeUtils.kstToUtc(originalKst);
        LocalDateTime backToKst = DateTimeUtils.utcToKst(utc);

        // then
        assertEquals(originalKst, backToKst);
    }

    @Test
    @DisplayName("null 입력 시 null 반환")
    void testNullInputs() {
        assertNull(DateTimeUtils.kstStringToUtc(null));
        assertNull(DateTimeUtils.utcToKstString(null));
        assertNull(DateTimeUtils.kstToUtc(null));
        assertNull(DateTimeUtils.utcToKst(null));
    }

    @Test
    @DisplayName("잘못된 날짜 형식 예외 발생")
    void testInvalidFormat() {
        String invalidStr = "2025/05/26 09:00:00"; // 잘못된 형식
        assertThrows(IllegalArgumentException.class, () -> DateTimeUtils.kstStringToUtc(invalidStr));
    }
}
