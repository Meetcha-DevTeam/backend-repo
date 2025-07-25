package com.meetcha.meetinglist.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlternativeVoteRequest {
    //대안 시간 투표 제출 요청
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime alternativeTime;
}
