package com.meetcha.meetinglist.dto;

import com.meetcha.global.util.DateTimeUtils;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlternativeTimeListResponse {

    private List<LocalDateTime> alternativeTimes;
    private LocalDateTime userSelectedTime;

    public static AlternativeTimeListResponse of(List<AlternativeTimeDto> dtoList) {
        // DB(LocalDateTime, UTC) → KST LocalDateTime 변환
        List<LocalDateTime> times = dtoList.stream()
                .map(dto -> DateTimeUtils.utcToKst(dto.getStartTime()))
                .toList();

        LocalDateTime selected = dtoList.stream()
                .filter(AlternativeTimeDto::isChecked)
                .map(dto -> DateTimeUtils.utcToKst(dto.getStartTime()))
                .findFirst()
                .orElse(null);

        return AlternativeTimeListResponse.builder()
                .alternativeTimes(times)
                .userSelectedTime(selected)
                .build();
    }
}
