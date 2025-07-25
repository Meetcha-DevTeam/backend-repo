package com.meetcha.meetinglist.dto;

import com.meetcha.meetinglist.domain.AlternativeTimeEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlternativeTimeDto {
    private LocalDateTime startTime;
    private boolean checked;

    public static AlternativeTimeDto from(AlternativeTimeEntity entity, int voteCount, boolean checked) {
        return AlternativeTimeDto.builder()
                .startTime(entity.getStartTime())
                .checked(checked)
                .build();
    }
}
