package com.meetcha.meeting.service;

import com.meetcha.joinmeeting.domain.ParticipantAvailability;
import com.meetcha.joinmeeting.domain.ParticipantAvailabilityRepository;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.meeting.domain.MeetingStatus;
import com.meetcha.meeting.service.algorithm.AlternativeTimeCalculator;
import com.meetcha.meeting.service.algorithm.Meeting;
import com.meetcha.meeting.service.algorithm.MeetingTimeCalculator;
import com.meetcha.meetinglist.domain.AlternativeTimeEntity;
import com.meetcha.meetinglist.repository.AlternativeTimeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingConfirmationServiceTest {

    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private ParticipantAvailabilityRepository availabilityRepository;
    @Mock
    private MeetingScheduleSyncService syncService;
    @Mock
    private AlternativeTimeRepository alternativeTimeRepository;

    @InjectMocks
    private MeetingConfirmationService service;

    /** 실제 ParticipantAvailability 엔티티 생성 헬퍼 */
    private ParticipantAvailability avail(
            UUID pid, UUID meetingId,
            LocalDateTime start, LocalDateTime end
    ) {
        return ParticipantAvailability.create(pid, meetingId, start, end);
    }

    @Test
    @DisplayName("최적 시간이 계산되면 확정 시간/상태를 BEFORE로 저장하고 캘린더를 동기화한다")
    void confirmMeeting_whenBestTimeExists_confirmsAndSyncs() {
        // given
        UUID meetingId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        MeetingEntity meeting = mock(MeetingEntity.class);

        when(meetingRepository.findByIdForUpdate(meetingId))
                .thenReturn(Optional.of(meeting));

        LocalDateTime s = LocalDateTime.now();
        LocalDateTime e = s.plusHours(2);
        List<ParticipantAvailability> availList =
                List.of(avail(participantId, meetingId, s, e));

        when(availabilityRepository.findByMeetingId(meetingId))
                .thenReturn(availList);

        Meeting algoMeeting = new Meeting(
                "m", List.of(), 0, 60,
                null, null, new ArrayList<>(), 0, List.of()
        );

        int bestStartMinutes = 9 * 60; // Day0 09:00
        LocalDateTime expectedBest =
                LocalDate.of(LocalDate.now().getYear(), 1, 1)
                        .atStartOfDay()
                        .plusMinutes(bestStartMinutes);

        try (MockedStatic<MeetingConverter> mc = mockStatic(MeetingConverter.class);
             MockedStatic<MeetingTimeCalculator> mt = mockStatic(MeetingTimeCalculator.class)) {

            mc.when(() -> MeetingConverter.toAlgorithmMeeting(meeting, availList))
                    .thenReturn(algoMeeting);

            mc.when(() -> MeetingConverter.toLocalDateTime(bestStartMinutes))
                    .thenReturn(expectedBest);

            mt.when(() -> MeetingTimeCalculator.calculateMeetingTime(algoMeeting))
                    .thenReturn(bestStartMinutes);

            // when
            service.confirmMeeting(meetingId);

            // then
            ArgumentCaptor<LocalDateTime> timeCap = ArgumentCaptor.forClass(LocalDateTime.class);
            verify(meeting).setConfirmedTime(timeCap.capture());
            assertEquals(expectedBest, timeCap.getValue());

            ArgumentCaptor<MeetingStatus> statusCap = ArgumentCaptor.forClass(MeetingStatus.class);
            verify(meeting).setMeetingStatus(statusCap.capture());
            assertEquals(MeetingStatus.BEFORE, statusCap.getValue());

            verify(meetingRepository).save(meeting);
            verify(syncService).syncMeetingToCalendars(meeting);

            verify(alternativeTimeRepository, never()).saveAll(anyList());
            verify(alternativeTimeRepository, never()).deleteByMeetingId(any());
        }
    }

    @Test
    @DisplayName("최적 시간이 없고 대안 후보가 있으면 후보 저장 + alternativeDeadline 설정 + MATCHING으로 저장한다")
    void confirmMeeting_whenNoBestTime_andAlternativesExist_setsMatchingAndDeadline() {
        // given
        UUID meetingId = UUID.randomUUID();
        UUID pid = UUID.randomUUID();
        MeetingEntity meeting = mock(MeetingEntity.class);

        when(meetingRepository.findByIdForUpdate(meetingId))
                .thenReturn(Optional.of(meeting));

        List<ParticipantAvailability> availList =
                List.of(avail(pid, meetingId,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusHours(1)));

        when(availabilityRepository.findByMeetingId(meetingId))
                .thenReturn(availList);

        Meeting algoMeeting = new Meeting(
                "m", List.of(), 0, 60,
                null, null, new ArrayList<>(), 0, List.of()
        );

        int year = LocalDate.now().getYear();

        LocalDateTime s1 = LocalDate.of(year, 1, 10).atTime(9, 0);
        LocalDateTime e1 = s1.plusMinutes(60);
        AlternativeTimeEntity alt1 = AlternativeTimeEntity.builder()
                .alternativeTimeId(UUID.randomUUID())
                .meetingId(meetingId)
                .startTime(s1)
                .endTime(e1)
                .durationAdjustedMinutes(60)
                .excludedParticipants(null)
                .build();

        LocalDateTime s2 = LocalDate.of(year, 1, 12).atTime(14, 0);
        LocalDateTime e2 = s2.plusMinutes(90);
        AlternativeTimeEntity alt2 = AlternativeTimeEntity.builder()
                .alternativeTimeId(UUID.randomUUID())
                .meetingId(meetingId)
                .startTime(s2)
                .endTime(e2)
                .durationAdjustedMinutes(90)
                .excludedParticipants("X,Y")
                .build();

        List<AlternativeTimeEntity> alterTimes = List.of(alt1, alt2);
        LocalDateTime expectedDeadline =
                s1.toLocalDate().minusDays(1).atTime(23, 59);

        try (MockedStatic<MeetingConverter> mc = mockStatic(MeetingConverter.class);
             MockedStatic<MeetingTimeCalculator> mt = mockStatic(MeetingTimeCalculator.class);
             MockedStatic<AlternativeTimeCalculator> ac = mockStatic(AlternativeTimeCalculator.class)) {

            mc.when(() -> MeetingConverter.toAlgorithmMeeting(meeting, availList))
                    .thenReturn(algoMeeting);

            mt.when(() -> MeetingTimeCalculator.calculateMeetingTime(algoMeeting))
                    .thenReturn(null); // 최적 시간 없음

            ac.when(() -> AlternativeTimeCalculator.getAlternativeTimes(algoMeeting, meetingId))
                    .thenReturn(alterTimes); // 대안 후보 있음

            // when
            service.confirmMeeting(meetingId);
        }

        // then
        verify(alternativeTimeRepository).deleteByMeetingId(meetingId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AlternativeTimeEntity>> listCap =
                ArgumentCaptor.forClass(List.class);
        verify(alternativeTimeRepository).saveAll(listCap.capture());
        verify(alternativeTimeRepository).flush();

        assertEquals(alterTimes.size(), listCap.getValue().size());

        ArgumentCaptor<LocalDateTime> deadlineCap = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(meeting).setAlternativeDeadline(deadlineCap.capture());
        assertEquals(expectedDeadline, deadlineCap.getValue());

        ArgumentCaptor<MeetingStatus> statusCap = ArgumentCaptor.forClass(MeetingStatus.class);
        verify(meeting).setMeetingStatus(statusCap.capture());
        assertEquals(MeetingStatus.MATCHING, statusCap.getValue());

        verify(meetingRepository).save(meeting);
        verify(syncService, never()).syncMeetingToCalendars(any());
    }

    @Test
    @DisplayName("최적 시간도 없고 대안 후보도 없으면 MATCH_FAILED로 저장한다")
    void confirmMeeting_whenNoBestTime_andNoAlternatives_setsMatchFailed() {
        // given
        UUID meetingId = UUID.randomUUID();
        UUID pid = UUID.randomUUID();
        MeetingEntity meeting = mock(MeetingEntity.class);

        when(meetingRepository.findByIdForUpdate(meetingId))
                .thenReturn(Optional.of(meeting));

        List<ParticipantAvailability> availList =
                List.of(avail(pid, meetingId,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusHours(1)));

        when(availabilityRepository.findByMeetingId(meetingId))
                .thenReturn(availList);

        Meeting algoMeeting = new Meeting(
                "m", List.of(), 0, 60,
                null, null, new ArrayList<>(), 0, List.of()
        );

        try (MockedStatic<MeetingConverter> mc = mockStatic(MeetingConverter.class);
             MockedStatic<MeetingTimeCalculator> mt = mockStatic(MeetingTimeCalculator.class);
             MockedStatic<AlternativeTimeCalculator> ac = mockStatic(AlternativeTimeCalculator.class)) {

            mc.when(() -> MeetingConverter.toAlgorithmMeeting(meeting, availList))
                    .thenReturn(algoMeeting);

            mt.when(() -> MeetingTimeCalculator.calculateMeetingTime(algoMeeting))
                    .thenReturn(null);

            ac.when(() -> AlternativeTimeCalculator.getAlternativeTimes(algoMeeting, meetingId))
                    .thenReturn(Collections.emptyList());

            // when
            service.confirmMeeting(meetingId);
        }

        // then
        ArgumentCaptor<MeetingStatus> statusCap = ArgumentCaptor.forClass(MeetingStatus.class);
        verify(meeting).setMeetingStatus(statusCap.capture());
        assertEquals(MeetingStatus.MATCH_FAILED, statusCap.getValue());

        verify(alternativeTimeRepository, never()).saveAll(anyList());
        verify(alternativeTimeRepository, never()).deleteByMeetingId(any());
        verify(syncService, never()).syncMeetingToCalendars(any());
        verify(meetingRepository).save(meeting);
    }

    @Test
    @DisplayName("가용 시간이 비어 있으면 MATCH_FAILED로 저장하고 나머지 로직은 타지 않는다")
    void confirmMeeting_whenNoAvailability_setsMatchFailed() {
        // given
        UUID meetingId = UUID.randomUUID();
        MeetingEntity meeting = mock(MeetingEntity.class);

        when(meetingRepository.findByIdForUpdate(meetingId))
                .thenReturn(Optional.of(meeting));
        when(availabilityRepository.findByMeetingId(meetingId))
                .thenReturn(Collections.emptyList());

        // when
        service.confirmMeeting(meetingId);

        // then
        ArgumentCaptor<MeetingStatus> statusCap = ArgumentCaptor.forClass(MeetingStatus.class);
        verify(meeting).setMeetingStatus(statusCap.capture());
        assertEquals(MeetingStatus.MATCH_FAILED, statusCap.getValue());

        verify(meetingRepository).save(meeting);
        verify(syncService, never()).syncMeetingToCalendars(any());
        verify(alternativeTimeRepository, never()).saveAll(anyList());
        verify(alternativeTimeRepository, never()).deleteByMeetingId(any());
    }
}
