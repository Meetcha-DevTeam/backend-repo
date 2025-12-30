package com.meetcha.meetinglist.service;

import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.joinmeeting.domain.MeetingParticipantRepository;
import com.meetcha.joinmeeting.domain.ParticipantAvailabilityRepository;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.meetinglist.domain.ParticipantAvailabilityEntity;
import com.meetcha.meetinglist.dto.MeetingAllAvailabilitiesResponse;
import com.meetcha.reflection.domain.MeetingReflectionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingListServiceTest {

    @Mock MeetingRepository meetingRepository;
    @Mock MeetingParticipantRepository meetingParticipantRepository;
    @Mock MeetingReflectionRepository reflectionRepository;
    @Mock ParticipantAvailabilityRepository participantAvailabilityRepository;

    @InjectMocks MeetingListService meetingListService;

    @Test
    @DisplayName("meetingId가 없으면 MEETING_NOT_FOUND 예외")
    void getAllParticipantsAvailabilities_meetingNotFound_throwException() {
        UUID meetingId = UUID.randomUUID();
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                meetingListService.getAllParticipantsAvailabilities(meetingId)
        ).isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.MEETING_NOT_FOUND);
                });

        verify(meetingRepository).findById(meetingId);
        verifyNoMoreInteractions(meetingParticipantRepository, participantAvailabilityRepository);
    }

    @Test
    @DisplayName("참여자가 0명이면 count=0, participants=[] 반환")
    void getAllParticipantsAvailabilities_noParticipants_returnsEmpty() {
        UUID meetingId = UUID.randomUUID();
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(MeetingEntity.builder().meetingId(meetingId).build()));
        when(meetingParticipantRepository.findParticipantIdsByMeetingId(meetingId)).thenReturn(List.of());

        MeetingAllAvailabilitiesResponse res =
                meetingListService.getAllParticipantsAvailabilities(meetingId);

        assertThat(res.getCount()).isZero();
        assertThat(res.getParticipants()).isEmpty();

        verify(participantAvailabilityRepository, never()).findAllByMeetingId(any());
    }

    @Test
    @DisplayName("참여자별 availabilities를 그룹핑하고, 미제출 참여자는 빈 리스트로 포함한다")
    void getAllParticipantsAvailabilities_someParticipants_someAvailabilities_groupedCorrectly() {
        UUID meetingId = UUID.randomUUID();

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        UUID p3 = UUID.randomUUID();

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(MeetingEntity.builder().meetingId(meetingId).build()));
        when(meetingParticipantRepository.findParticipantIdsByMeetingId(meetingId)).thenReturn(List.of(p1, p2, p3));

        // p1: 2개, p2: 1개, p3: 0개
        ParticipantAvailabilityEntity a1 = ParticipantAvailabilityEntity.builder()
                .availabilityId(UUID.randomUUID())
                .participantId(p1)
                .startAt(LocalDateTime.of(2025, 7, 22, 6, 0))
                .endAt(LocalDateTime.of(2025, 7, 22, 6, 30))
                .build();

        ParticipantAvailabilityEntity a2 = ParticipantAvailabilityEntity.builder()
                .availabilityId(UUID.randomUUID())
                .participantId(p1)
                .startAt(LocalDateTime.of(2025, 7, 22, 7, 0))
                .endAt(LocalDateTime.of(2025, 7, 22, 7, 30))
                .build();

        ParticipantAvailabilityEntity b1 = ParticipantAvailabilityEntity.builder()
                .availabilityId(UUID.randomUUID())
                .participantId(p2)
                .startAt(LocalDateTime.of(2025, 7, 22, 6, 0))
                .endAt(LocalDateTime.of(2025, 7, 22, 7, 0))
                .build();

        when(participantAvailabilityRepository.findAllByMeetingId(meetingId))
                .thenReturn(List.of(a1, a2, b1));

        MeetingAllAvailabilitiesResponse res =
                meetingListService.getAllParticipantsAvailabilities(meetingId);

        assertThat(res.getCount()).isEqualTo(3);
        assertThat(res.getParticipants()).hasSize(3);

        // participantId별 결과 찾기
        var map = res.getParticipants().stream()
                .collect(java.util.stream.Collectors.toMap(
                        MeetingAllAvailabilitiesResponse.ParticipantAvailabilities::getParticipantId,
                        MeetingAllAvailabilitiesResponse.ParticipantAvailabilities::getAvailabilities
                ));

        assertThat(map.get(p1)).hasSize(2);
        assertThat(map.get(p2)).hasSize(1);
        assertThat(map.get(p3)).isEmpty(); // 제출 안 한 참여자도 []로 내려감
    }
}
