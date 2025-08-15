package com.meetcha.global.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 *  LocalDateTime <-> String 변환 및 타임존 변환을 위한 유틸 클래스
 */
public class DateTimeUtil {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_PATTERN);
    private static final ZoneId ZONE_KST = ZoneId.of("Asia/Seoul");
    private static final ZoneId ZONE_UTC = ZoneOffset.UTC;

    /**
     * 문자열을 LocalDateTime(KST 기준)으로 변환
     * - 지원 형식:
     *   yyyy-MM-dd HH:mm:ss
     *   yyyy-MM-dd'T'HH:mm:ss
     *   yyyy-MM-dd'T'HH:mm:ssZ (UTC/Zulu)
     */
    public static LocalDateTime toLocalDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        try {
            // 기본 공백 구분 패턴
            return LocalDateTime.parse(dateTimeStr, FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // ISO_LOCAL_DATE_TIME (T 구분)
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e2) {
                try {
                    // ISO_INSTANT (Z, UTC 표기)
                    Instant instant = Instant.parse(dateTimeStr);
                    return instant.atZone(ZONE_KST).toLocalDateTime(); // KST 변환
                } catch (DateTimeParseException e3) {
                    throw new IllegalArgumentException(
                            "잘못된 날짜 형식입니다. 지원 형식: 'yyyy-MM-dd HH:mm:ss', ISO_LOCAL_DATE_TIME, ISO_INSTANT(Z 표기)",
                            e3
                    );
                }
            }
        }
    }

    /**
     * LocalDateTime을 문자열로 변환 (기본 패턴)
     */
    public static String toString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(FORMATTER);
    }

    /**
     * UTC → KST 변환
     */
    public static LocalDateTime utcToKst(LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return null;
        }
        ZonedDateTime utcZoned = utcDateTime.atZone(ZONE_UTC);
        return utcZoned.withZoneSameInstant(ZONE_KST).toLocalDateTime();
    }

    /**
     * KST → UTC 변환
     */
    public static LocalDateTime kstToUtc(LocalDateTime kstDateTime) {
        if (kstDateTime == null) {
            return null;
        }
        ZonedDateTime kstZoned = kstDateTime.atZone(ZONE_KST);
        return kstZoned.withZoneSameInstant(ZONE_UTC).toLocalDateTime();
    }
}
