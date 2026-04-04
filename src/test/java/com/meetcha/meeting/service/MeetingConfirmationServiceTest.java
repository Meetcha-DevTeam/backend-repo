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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

    private MeetingEntity meeting;

    @BeforeEach
    void setUp() {
        meeting = mock(MeetingEntity.class);
        lenient().when(meeting.getDeadline()).thenReturn(null);
        lenient().when(meeting.getConfirmedTime()).thenReturn(null);
        lenient().when(meeting.getDurationMinutes()).thenReturn(60);
    }

    private ParticipantAvailability avail(UUID pid, UUID meetingId,
                                          LocalDateTime start, LocalDateTime end) {
        return ParticipantAvailability.create(pid, meetingId, start, end);
    }

    private Meeting createAlgoMeeting() {
        return new Meeting(
                "m", List.of(), 0, 60,
                null, null, new ArrayList<>(), 0, List.of()
        );
    }

    private List<ParticipantAvailability> twoParticipants(UUID meetingId) {
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        LocalDateTime s = LocalDateTime.now();
        LocalDateTime e = s.plusHours(2);

        return List.of(
                avail(p1, meetingId, s, e),
                avail(p2, meetingId, s, e)
        );
    }

    // =========================================================
    // 1. BEST TIME 존재 → 확정
    // =========================================================
    @Test
    @DisplayName("최적 시간이 있으면 확정 + BEFORE + sync")
    void confirmMeeting_whenBestTimeExists_confirmsAndSyncs() {

        UUID meetingId = UUID.randomUUID();

        when(meetingRepository.findByIdForUpdate(meetingId))
                .thenReturn(Optional.of(meeting));

        List<ParticipantAvailability> availList = twoParticipants(meetingId);

        when(availabilityRepository.findByMeetingId(meetingId))
                .thenReturn(availList);

        Meeting algoMeeting = createAlgoMeeting();

        int bestStartMinutes = 9 * 60;
        LocalDateTime expected =
                LocalDate.of(LocalDate.now().getYear(), 1, 1)
                        .atStartOfDay()
                        .plusMinutes(bestStartMinutes);

        try (MockedStatic<MeetingConverter> mc = mockStatic(MeetingConverter.class);
             MockedStatic<MeetingTimeCalculator> mt = mockStatic(MeetingTimeCalculator.class)) {

            mc.when(() -> MeetingConverter.toAlgorithmMeeting(any(), any()))
                    .thenReturn(algoMeeting);

            mc.when(() -> MeetingConverter.toLocalDateTime(bestStartMinutes))
                    .thenReturn(expected);

            mt.when(() -> MeetingTimeCalculator.calculateMeetingTime(any()))
                    .thenReturn(bestStartMinutes);

            mt.when(() -> MeetingTimeCalculator.getMaxContinuousMinutes(any()))
                    .thenReturn(60);

            service.confirmMeeting(meetingId);

            ArgumentCaptor<LocalDateTime> timeCap = ArgumentCaptor.forClass(LocalDateTime.class);
            verify(meeting).setConfirmedTime(timeCap.capture());
            assertEquals(expected, timeCap.getValue());

            verify(meeting).setMeetingStatus(MeetingStatus.BEFORE);
            verify(syncService).syncMeetingToCalendars(meeting);
            verify(meetingRepository).save(meeting);
        }
    }

    // =========================================================
    // 2. BEST 없음 + 대안 있음 → MATCHING
    // =========================================================
    @Test
    @DisplayName("최적 없음 + 대안 있음 → MATCHING")
    void confirmMeeting_whenNoBestTime_andAlternativesExist_setsMatching() {

        UUID meetingId = UUID.randomUUID();

        when(meetingRepository.findByIdForUpdate(meetingId))
                .thenReturn(Optional.of(meeting));

        List<ParticipantAvailability> availList = twoParticipants(meetingId);

        when(availabilityRepository.findByMeetingId(meetingId))
                .thenReturn(availList);

        Meeting algoMeeting = createAlgoMeeting();

        AlternativeTimeEntity alt = AlternativeTimeEntity.builder()
                .alternativeTimeId(UUID.randomUUID())
                .meetingId(meetingId)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .build();

        try (MockedStatic<MeetingConverter> mc = mockStatic(MeetingConverter.class);
             MockedStatic<MeetingTimeCalculator> mt = mockStatic(MeetingTimeCalculator.class);
             MockedStatic<AlternativeTimeCalculator> ac = mockStatic(AlternativeTimeCalculator.class)) {

            mc.when(() -> MeetingConverter.toAlgorithmMeeting(any(), any()))
                    .thenReturn(algoMeeting);

            mt.when(() -> MeetingTimeCalculator.calculateMeetingTime(any()))
                    .thenReturn(null);

            mt.when(() -> MeetingTimeCalculator.getMaxContinuousMinutes(any()))
                    .thenReturn(60);

            ac.when(() -> AlternativeTimeCalculator.getAlternativeTimes(any(), any()))
                    .thenReturn(List.of(alt));

            service.confirmMeeting(meetingId);
        }

        verify(alternativeTimeRepository).deleteByMeetingId(meetingId);
        verify(alternativeTimeRepository).saveAll(any());
        verify(meeting).setMeetingStatus(MeetingStatus.MATCHING);
        verify(meetingRepository).save(meeting);
    }

    // =========================================================
    // 3. BEST 없음 + 대안 없음 → MATCH_FAILED
    // =========================================================
    @Test
    @DisplayName("최적 없음 + 대안 없음 → MATCH_FAILED")
    void confirmMeeting_whenNoBestTime_andNoAlternatives_setsMatchFailed() {

        UUID meetingId = UUID.randomUUID();

        when(meetingRepository.findByIdForUpdate(meetingId))
                .thenReturn(Optional.of(meeting));

        when(availabilityRepository.findByMeetingId(meetingId))
                .thenReturn(twoParticipants(meetingId));

        Meeting algoMeeting = createAlgoMeeting();

        try (MockedStatic<MeetingConverter> mc = mockStatic(MeetingConverter.class);
             MockedStatic<MeetingTimeCalculator> mt = mockStatic(MeetingTimeCalculator.class);
             MockedStatic<AlternativeTimeCalculator> ac = mockStatic(AlternativeTimeCalculator.class)) {

            mc.when(() -> MeetingConverter.toAlgorithmMeeting(any(), any()))
                    .thenReturn(algoMeeting);

            mt.when(() -> MeetingTimeCalculator.calculateMeetingTime(any()))
                    .thenReturn(null);

            mt.when(() -> MeetingTimeCalculator.getMaxContinuousMinutes(any()))
                    .thenReturn(60);

            ac.when(() -> AlternativeTimeCalculator.getAlternativeTimes(any(), any()))
                    .thenReturn(Collections.emptyList());

            service.confirmMeeting(meetingId);
        }

        verify(meeting).setMeetingStatus(MeetingStatus.MATCH_FAILED);
        verify(meetingRepository).save(meeting);
    }

    // =========================================================
    // 4. 가용 시간 없음 → MATCH_FAILED
    // =========================================================
    @Test
    @DisplayName("가용 시간 없음 → MATCH_FAILED")
    void confirmMeeting_whenNoAvailability_setsMatchFailed() {

        UUID meetingId = UUID.randomUUID();

        when(meetingRepository.findByIdForUpdate(meetingId))
                .thenReturn(Optional.of(meeting));

        when(availabilityRepository.findByMeetingId(meetingId))
                .thenReturn(Collections.emptyList());

        service.confirmMeeting(meetingId);

        verify(meeting).setMeetingStatus(MeetingStatus.MATCH_FAILED);
        verify(meetingRepository).save(meeting);
    }
}