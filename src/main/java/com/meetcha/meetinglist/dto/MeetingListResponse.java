package com.meetcha.meetinglist.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.meetcha.global.util.DateTimeUtils;
import com.meetcha.meeting.domain.MeetingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingListResponse {

    private UUID meetingId;
    private String title;

    @JsonIgnore
    private LocalDateTime deadline; // UTC

    @JsonIgnore
    private LocalDateTime confirmedTime; // UTC

    private int durationMinutes;
    private MeetingStatus meetingStatus;

    @JsonProperty("deadline")
    public String getDeadlineKst() {
        return DateTimeUtils.utcToKstString(this.deadline);
    }

    @JsonProperty("confirmedTime")
    public String getConfirmedTimeKst() {
        return DateTimeUtils.utcToKstString(this.confirmedTime);
    }
}
