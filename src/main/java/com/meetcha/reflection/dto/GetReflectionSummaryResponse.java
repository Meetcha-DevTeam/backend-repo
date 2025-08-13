package com.meetcha.reflection.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetReflectionSummaryResponse {
    private long totalReflections;
    private int averageContribution;
    private String mostFrequentRole;
}
