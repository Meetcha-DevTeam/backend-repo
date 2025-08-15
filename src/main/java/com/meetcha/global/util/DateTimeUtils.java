package com.meetcha.global.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * LocalDateTime <-> String 변환 및 타임존 변환 유틸 (KST ↔ UTC)
 * - 프론트: KST 형식("yyyy-MM-dd'T'HH:mm:ss")으로 요청/응답
 * - DB: UTC(LocalDateTime)로 저장
 */
public class DateTimeUtils {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_PATTERN);
    private static final ZoneId ZONE_KST = ZoneId.of("Asia/Seoul");
    private static final ZoneId ZONE_UTC = ZoneOffset.UTC;

    /**
     * 프론트에서 받은 KST 문자열 → DB 저장용 UTC LocalDateTime 변환
     * (백엔드 수신 시 -9시간)
     */
    public static LocalDateTime kstStringToUtc(String kstDateTimeStr) {
        if (kstDateTimeStr == null || kstDateTimeStr.isBlank()) {
            return null;
        }
        try {
            LocalDateTime kstDateTime = LocalDateTime.parse(kstDateTimeStr, FORMATTER);
            return kstToUtc(kstDateTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("잘못된 날짜 형식입니다. 지원 형식: 'yyyy-MM-dd'T'HH:mm:ss'", e);
        }
    }

    /**
     * DB 저장된 UTC LocalDateTime → 프론트 응답용 KST 문자열 변환
     * (응답 시 +9시간)
     */
    public static String utcToKstString(LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return null;
        }
        return utcToKst(utcDateTime).format(FORMATTER);
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
