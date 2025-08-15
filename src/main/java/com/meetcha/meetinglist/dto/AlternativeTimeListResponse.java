package com.meetcha.meetinglist.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.meetcha.global.util.DateTimeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlternativeTimeListResponse {

    @JsonIgnore
    private List<LocalDateTime> alternativeTimes; // UTC

    @JsonIgnore
    private LocalDateTime userSelectedTime; // UTC

    @JsonProperty("alternativeTimes")
    public List<String> getAlternativeTimesKst() {
        return alternativeTimes.stream()
                .map(DateTimeUtils::utcToKstString)
                .toList();
    }

    @JsonProperty("userSelectedTime")
    public String getUserSelectedTimeKst() {
        return DateTimeUtils.utcToKstString(this.userSelectedTime);
    }

    public static AlternativeTimeListResponse of(List<AlternativeTimeDto> dtoList) {
        List<LocalDateTime> times = dtoList.stream()
                .map(AlternativeTimeDto::getStartTime)
                .toList();

        LocalDateTime selected = dtoList.stream()
                .filter(AlternativeTimeDto::isChecked)
                .map(AlternativeTimeDto::getStartTime)
                .findFirst()
                .orElse(null);

        return new AlternativeTimeListResponse(times, selected);
    }
}
