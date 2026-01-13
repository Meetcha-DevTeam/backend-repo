package com.meetcha.meetinglist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class MeetingAllAvailabilitiesResponse {

    private List<ParticipantAvailabilities> participants;
    private int count;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ParticipantAvailabilities {
        private UUID participantId;
        private List<Availability> availabilities;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Availability {
        private UUID availabilityId;
        private LocalDateTime startAt;
        private LocalDateTime endAt;
    }
}