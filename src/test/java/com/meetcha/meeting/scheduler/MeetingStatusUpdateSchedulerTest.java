package com.meetcha.meeting.scheduler;

import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.meeting.domain.MeetingStatus;
import com.meetcha.meeting.service.MeetingScheduleSyncService;
import com.meetcha.meetinglist.domain.AlternativeTimeEntity;
import com.meetcha.meetinglist.repository.AlternativeTimeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingStatusUpdateSchedulerTest {

    @Mock private MeetingRepository meetingRepository;
    @Mock private AlternativeTimeRepository alternativeTimeRepository;
    @Mock private MeetingScheduleSyncService syncService;

    @InjectMocks
    private MeetingStatusUpdateScheduler scheduler;

    @Test
    @DisplayName("대안 시간이 존재하면 확정 시간/상태를 저장하고 구글 캘린더를 동기화한다")
    void confirmFromAlternativeTimes_whenAlternativeExists_confirmsAndSyncs() {
        // given
        UUID meetingId = UUID.randomUUID();
        MeetingEntity meeting = mock(MeetingEntity.class);
        when(meeting.getMeetingId()).thenReturn(meetingId);

        LocalDateTime confirmedStart = LocalDateTime.now().plusDays(1);
        AlternativeTimeEntity alt = AlternativeTimeEntity.builder()
                .alternativeTimeId(UUID.randomUUID())
                .meetingId(meetingId)
                .startTime(confirmedStart)
                .endTime(confirmedStart.plusHours(1))
                .durationAdjustedMinutes(60)
                .build();

        when(meetingRepository.findMeetingsToConfirmFromAlternative())
                .thenReturn(List.of(meeting));
        when(alternativeTimeRepository.findTopByMeetingIdOrderByVoteCountDescStartTimeAsc(meetingId))
                .thenReturn(Optional.of(alt));

        // when
        scheduler.confirmFromAlternativeTimes();

        // then
        // setConfirmedTime & setMeetingStatus 검증
        ArgumentCaptor<LocalDateTime> timeCap = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(meeting).setConfirmedTime(timeCap.capture());
        assertEquals(confirmedStart, timeCap.getValue());

        ArgumentCaptor<MeetingStatus> statusCap = ArgumentCaptor.forClass(MeetingStatus.class);
        verify(meeting).setMeetingStatus(statusCap.capture());
        assertEquals(MeetingStatus.BEFORE, statusCap.getValue());

        // 저장 및 동기화 검증
        verify(meetingRepository).save(meeting);
        verify(syncService).syncMeetingToCalendars(meeting);
    }

}
