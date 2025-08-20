package com.meetcha.meetinglist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.meetcha.global.util.DateTimeUtils;
import com.meetcha.meetinglist.domain.AlternativeTimeEntity;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlternativeTimeDto {
    @JsonIgnore
    private LocalDateTime startTime;
    @JsonIgnore
    private LocalDateTime endTime;
  
    private Integer adjustedDurationMinutes;
    private List<String> excludedUserNames;
    private List<String> includedUserNames;
    private boolean checked;

    // 응답에는 KST 문자열을 노출
    @JsonProperty("startTime")
    public String getStartTimeKst() {
        return DateTimeUtils.utcToKstString(this.startTime);
    }

    @JsonProperty("endTime")
    public String getEndTimeKst() {
        return DateTimeUtils.utcToKstString(this.endTime);
    }

    // 내부 계산/선택용으로 UTC 원본을 노출 (ListResponse.of 에서 사용)
    @JsonIgnore
    public LocalDateTime getStartTimeUtc() {
        return startTime;
    }

    public static AlternativeTimeDto from(AlternativeTimeEntity entity, int voteCount, boolean checked, List<String> excludedNames, List<String> includedUserNames) {
        return AlternativeTimeDto.builder()
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .adjustedDurationMinutes(entity.getDurationAdjustedMinutes())
                .excludedUserNames(excludedNames)
                .includedUserNames(includedUserNames)
                .checked(checked)
                .build();
    }
}
