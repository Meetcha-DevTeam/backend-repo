package com.meetcha.meetinglist.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.meetcha.global.util.DateTimeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NeedReflectionResponse {
    private UUID meetingId;
    private String title;
    private String description;
    private UUID projectId;
    private String projectName;

    @JsonIgnore
    private LocalDateTime confirmedTime; // UTC

    private String meetingStatus;

    @JsonProperty("confirmedTime")
    public String getConfirmedTimeKst() {
        return DateTimeUtils.utcToKstString(this.confirmedTime);
    }
}
