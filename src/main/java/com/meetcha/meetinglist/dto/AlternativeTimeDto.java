package com.meetcha.meetinglist.dto;

import com.meetcha.global.util.DateTimeUtils;
import com.meetcha.meetinglist.domain.AlternativeTimeEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlternativeTimeDto {
    private String startTime;
    private String endTime;
    private Integer adjustedDurationMinutes;
    private List<String> excludedUserNames;
    private List<String> includedUserNames;
    private boolean checked;

    public static AlternativeTimeDto from(AlternativeTimeEntity entity, int voteCount, boolean checked, List<String> excludedNames, List<String> includedUserNames) {
        return AlternativeTimeDto.builder()
                .startTime(DateTimeUtils.utcToKstString(entity.getStartTime()))
                .endTime(DateTimeUtils.utcToKstString(entity.getEndTime()))
                .adjustedDurationMinutes(entity.getDurationAdjustedMinutes())
                .excludedUserNames(excludedNames)
                .includedUserNames(includedUserNames)
                .checked(checked)
                .build();
    }
}
