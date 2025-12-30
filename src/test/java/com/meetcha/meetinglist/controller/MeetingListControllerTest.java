package com.meetcha.meetinglist.controller;

import com.meetcha.joinmeeting.service.JoinMeetingService;
import com.meetcha.meetinglist.dto.MeetingAllAvailabilitiesResponse;
import com.meetcha.meetinglist.service.AlternativeTimeService;
import com.meetcha.meetinglist.service.MeetingListService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeetingListController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class MeetingListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeetingListService meetingListService;

    @MockitoBean
    private AlternativeTimeService alternativeTimeService;

    @MockitoBean
    private JoinMeetingService joinMeetingService;

    @Test
    void getAllAvailabilities_returnsExpectedJson() throws Exception {
        UUID meetingId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        MeetingAllAvailabilitiesResponse response =
                MeetingAllAvailabilitiesResponse.builder()
                        .participants(List.of(
                                MeetingAllAvailabilitiesResponse.ParticipantAvailabilities.builder()
                                        .participantId(participantId)
                                        .availabilities(List.of(
                                                MeetingAllAvailabilitiesResponse.Availability.builder()
                                                        .availabilityId(UUID.randomUUID())
                                                        .startAt(LocalDateTime.of(2025, 7, 22, 15, 0))
                                                        .endAt(LocalDateTime.of(2025, 7, 22, 15, 30))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .count(1)
                        .build();

        when(meetingListService.getAllParticipantsAvailabilities(eq(meetingId)))
                .thenReturn(response);

        mockMvc.perform(get("/meeting-lists/{meetingId}/availabilities", meetingId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(1))
                .andExpect(jsonPath("$.data.participants[0].participantId").value(participantId.toString()));
    }
}
