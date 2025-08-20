package com.meetcha.joinmeeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetSelectedTime {
    private String startAt;
    private String endAt;
}