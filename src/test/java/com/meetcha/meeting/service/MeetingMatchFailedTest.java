package com.meetcha.meeting.service;

import com.meetcha.joinmeeting.domain.ParticipantAvailability;
import com.meetcha.joinmeeting.domain.ParticipantAvailabilityRepository;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.meeting.domain.MeetingStatus;
import com.meetcha.meeting.scheduler.MeetingStatusUpdateScheduler;
import com.meetcha.meeting.service.MeetingConfirmationService;
import com.meetcha.meeting.service.MeetingScheduleSyncService;
import com.meetcha.meeting.service.algorithm.AlternativeTimeCalculator;
import com.meetcha.meeting.service.algorithm.Meeting;
import com.meetcha.meeting.service.MeetingConverter;
import com.meetcha.meeting.service.algorithm.MeetingTimeCalculator;
import com.meetcha.meetinglist.domain.AlternativeTimeEntity;
import com.meetcha.meetinglist.repository.AlternativeTimeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingMatchFailedTest {

    // 공통 mock
    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private ParticipantAvailabilityRepository availabilityRepository;
    @Mock
    private AlternativeTimeRepository alternativeTimeRepository;
    @Mock
    private MeetingScheduleSyncService syncService;

    // 확인 서비스
    @InjectMocks
    private MeetingConfirmationService confirmationService;

    // 스케줄러
    @InjectMocks
    private MeetingStatusUpdateScheduler scheduler;

    private ParticipantAvailability avail(UUID pid, UUID meetingId,
                                          LocalDateTime start, LocalDateTime end) {
        return ParticipantAvailability.create(pid, meetingId, start, end);
    }

    @Test
    @DisplayName("① 가용 시간이 0개이면 바로 MATCH_FAILED 로 저장된다")
    void confirmMeeting_whenNoAvailability_setsMatchFailed() {
        // given
        UUID meetingId = UUID.randomUUID();
        MeetingEntity meeting = mock(MeetingEntity.class);

        when(meetingRepository.findByIdForUpdate(meetingId))
                .thenReturn(Optional.of(meeting));
        when(availabilityRepository.findByMeetingId(meetingId))
                .thenReturn(Collections.emptyList());

        // when
        confirmationService.confirmMeeting(meetingId);

        // then
        ArgumentCaptor<MeetingStatus> statusCap = ArgumentCaptor.forClass(MeetingStatus.class);
        verify(meeting).setMeetingStatus(statusCap.capture());
        assertEquals(MeetingStatus.MATCH_FAILED, statusCap.getValue());

        verify(meetingRepository).save(meeting);
        verifyNoInteractions(syncService);
        verifyNoInteractions(alternativeTimeRepository);
    }

    @Test
    @DisplayName("② 최적 시간도 없고 대안 후보도 없으면 MATCH_FAILED 로 저장된다")
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
                    .thenReturn(null); // 1차 알고리즘 실패

            ac.when(() -> AlternativeTimeCalculator.getAlternativeTimes(algoMeeting, meetingId))
                    .thenReturn(Collections.emptyList()); // 대안 후보도 없음

            // when
            confirmationService.confirmMeeting(meetingId);
        }

        // then
        ArgumentCaptor<MeetingStatus> statusCap = ArgumentCaptor.forClass(MeetingStatus.class);
        verify(meeting).setMeetingStatus(statusCap.capture());
        assertEquals(MeetingStatus.MATCH_FAILED, statusCap.getValue());

        verify(alternativeTimeRepository, never()).saveAll(anyList());
        verify(alternativeTimeRepository, never()).deleteByMeetingId(any());
        verifyNoInteractions(syncService);
        verify(meetingRepository).save(meeting);
    }

    @Test
    @DisplayName("③ 대안 시간 투표 마감 후 투표 결과가 없으면 MATCH_FAILED 로 저장된다")
    void confirmFromAlternativeTimes_whenNoVotes_setsMatchFailed() {
        // given
        UUID meetingId = UUID.randomUUID();
        MeetingEntity meeting = mock(MeetingEntity.class);
        when(meeting.getMeetingId()).thenReturn(meetingId);

        // 마감된 미팅 하나 반환
        when(meetingRepository.findMeetingsToConfirmFromAlternativeForUpdate(any(LocalDateTime.class)))
                .thenReturn(List.of(meeting));

        // 최다 득표 대안 시간 없음(투표 0건)
        when(alternativeTimeRepository.findTopByMeetingIdOrderByVoteCountDescStartTimeAsc(meetingId))
                .thenReturn(Optional.empty());

        // when
        scheduler.confirmFromAlternativeTimes();

        // then
        ArgumentCaptor<MeetingStatus> statusCap = ArgumentCaptor.forClass(MeetingStatus.class);
        verify(meeting).setMeetingStatus(statusCap.capture());
        assertEquals(MeetingStatus.MATCH_FAILED, statusCap.getValue());

        verify(meetingRepository).save(meeting);
        verifyNoInteractions(syncService);
    }
}
