package com.meetcha.user.util;

import com.meetcha.user.domain.UnavailableTime;
import com.meetcha.user.dto.BusyTimeResponse;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UnavailableTimeConverter {
    //LocalTime-> LocalDateTime 변환
    public static List<BusyTimeResponse> convertToBusyTimes(
            List<UnavailableTime> unavailableTimes,
            LocalDateTime from,
            LocalDateTime to
    ) {
        List<BusyTimeResponse> busyTimes = new ArrayList<>();

        LocalDate cursor = from.toLocalDate();
        LocalDate endDate = to.toLocalDate();

        while (!cursor.isAfter(endDate)) {
            DayOfWeek currentDay = cursor.getDayOfWeek();

            for (UnavailableTime rule : unavailableTimes) {
                if (rule.getDayOfWeek() == currentDay) {
                    LocalDateTime start = cursor.atTime(rule.getStartTime());
                    LocalDateTime end = cursor.atTime(rule.getEndTime());

                    // 범위 체크
                    if (start.isBefore(to) && end.isAfter(from)) {
                        busyTimes.add(new BusyTimeResponse(start, end));
                    }
                }
            }

            cursor = cursor.plusDays(1);
        }

        return busyTimes;
    }
}
