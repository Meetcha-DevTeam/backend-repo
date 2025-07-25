/*package com.meetcha.meetinglist.dto;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlternativeTimeListResponse {

    private List<ZonedDateTime> alternativeTimes;
    private ZonedDateTime userSelectedTime;

    public static AlternativeTimeListResponse of(List<AlternativeTimeDto> dtoList) {
        List<ZonedDateTime> times = dtoList.stream()
                .map(dto -> dto.getStartTime().atZone(java.time.ZoneId.of("UTC")))  // ISO 8601 포맷 보장
                .toList();

        ZonedDateTime selected = dtoList.stream()
                .filter(AlternativeTimeDto::isChecked)
                .map(dto -> dto.getStartTime().atZone(java.time.ZoneId.of("UTC")))
                .findFirst()
                .orElse(null);

        return AlternativeTimeListResponse.builder()
                .alternativeTimes(times)
                .userSelectedTime(selected)
                .build();
    }
}
*/