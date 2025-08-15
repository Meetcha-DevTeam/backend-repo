package com.meetcha.meetinglist.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlternativeVoteRequest {
    //대안 시간 투표 제출 요청
    private LocalDateTime alternativeTime;
}
