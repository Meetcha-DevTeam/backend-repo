package com.meetcha.external.google;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

public class RecurrenceUtils {

    private RecurrenceUtils() {} // 인스턴스화 방지

    public static String parseRecurrenceToLabel(List<String> recurrenceList) {
        if (recurrenceList == null || recurrenceList.isEmpty()) return "NONE";

        String rule = recurrenceList.get(0); // 예: RRULE:FREQ=WEEKLY;BYDAY=MO,WE,FR

        if (rule.contains("FREQ=DAILY")) return "매일";
        if (rule.contains("FREQ=WEEKLY")) {
            if (rule.contains("BYDAY=MO,WE,FR")) return "매주 월수금";
            if (rule.contains("BYDAY=MO,TU,WE,TH,FR")) return "매주 평일";
            return "매주";
        }
        if (rule.contains("FREQ=MONTHLY")) return "매월";
        return "반복";
    }

    public static String buildGoogleRRule(String recurrence, LocalDateTime startAt) {
        if (recurrence == null || recurrence.equalsIgnoreCase("NONE")) return null;

        DayOfWeek day = startAt.getDayOfWeek(); // 반복 기준 요일
        String weekday = day.toString().substring(0, 2); // "MON", "TUE" → "MO", "TU"

        return switch (recurrence) {
            case "DAILY" -> "RRULE:FREQ=DAILY";
            case "WEEKLY" -> "RRULE:FREQ=WEEKLY;BYDAY=" + weekday;
            case "BIWEEKLY" -> "RRULE:FREQ=WEEKLY;INTERVAL=2;BYDAY=" + weekday;
            case "MONTHLY" -> "RRULE:FREQ=MONTHLY";
            default -> null;
        };
    }

}
