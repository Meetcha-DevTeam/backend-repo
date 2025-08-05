package com.meetcha.meetinglist.dto;

import com.meetcha.meetinglist.domain.AlternativeTimeEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlternativeTimeDto {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer adjustedDurationMinutes;
    private List<String> excludedUserNames;
    private List<String> includedUserNames;
    private boolean checked;

    public static AlternativeTimeDto from(AlternativeTimeEntity entity, int voteCount, boolean checked, List<String> excludedNames) {
        return AlternativeTimeDto.builder()
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .adjustedDurationMinutes(entity.getDurationAdjustedMinutes())
                .excludedUserNames(excludedNames)
                .checked(checked)
                .build();
    }
}
