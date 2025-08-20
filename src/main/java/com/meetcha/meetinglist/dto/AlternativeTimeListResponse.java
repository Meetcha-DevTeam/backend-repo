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

    private List<AlternativeTimeDto> alternativeTimes;

    @JsonIgnore
    private LocalDateTime userSelectedTime; // UTC

    @JsonProperty("userSelectedTime")
    public String getUserSelectedTimeKst() {
        return DateTimeUtils.utcToKstString(this.userSelectedTime);
    }

    public static AlternativeTimeListResponse of(List<AlternativeTimeDto> dtoList) {
        // 체크된 시간(선택된 시간)을 찾아냄
        LocalDateTime selected = dtoList.stream()
                .filter(AlternativeTimeDto::isChecked)
                .map(dto -> DateTimeUtils.kstStringToUtc(dto.getStartTime()))
                .findFirst()
                .orElse(null);

        // alternativeTimes는 그대로 dtoList를 사용
        return new AlternativeTimeListResponse(dtoList, selected);
    }
}
