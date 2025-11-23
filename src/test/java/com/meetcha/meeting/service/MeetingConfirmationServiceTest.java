package com.meetcha.meeting.service;

import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.joinmeeting.domain.MeetingParticipantRepository;
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
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@ExtendWith(MockitoExtension.class)
class MeetingConfirmationServiceTest {

    @Mock private MeetingRepository meetingRepository;
    @Mock private MeetingParticipantRepository participantRepository;
    @Mock private ParticipantAvailabilityRepository availabilityRepository;
    @Mock private MeetingScheduleSyncService syncService;
    @Mock private AlternativeTimeRepository alternativeTimeRepository;

    @InjectMocks
    private MeetingConfirmationService service;

    /* 실제 엔티티 생성 헬퍼 */

private ParticipantAvailability avail(UUID pid, UUID meetingId, LocalDateTime start, LocalDateTime end) {
        return ParticipantAvailability.create(pid, meetingId, start, end);
    }

    @Test
    @DisplayName("최적 시간이 계산되면 확정 시간/상태를 저장하고 캘린더를 동기화한다")
    void confirmMeeting_whenBestTimeExists_confirmsAndSyncs() {
        // given
        UUID meetingId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        MeetingEntity meeting = mock(MeetingEntity.class);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        // 최소 1건의 가용 시간(실제 엔티티)
        LocalDateTime s = LocalDateTime.now();
        LocalDateTime e = s.plusHours(2);
        when(availabilityRepository.findByMeetingId(meetingId))
                .thenReturn(List.of(avail(participantId, meetingId, s, e)));

        // 정적 메서드 결과 고정
        Meeting algoMeeting = new Meeting("m", List.of(), 0, 60, null, null, List.of(), 0, List.of());
        int bestStartMinutes = 9 * 60; // Day0 09:00
        LocalDateTime expectedBest =
                LocalDate.of(LocalDate.now().getYear(), 1, 1).atStartOfDay().plusMinutes(bestStartMinutes);

        try (MockedStatic<MeetingConverter> mc = mockStatic(MeetingConverter.class);
             MockedStatic<MeetingTimeCalculator> mt = mockStatic(MeetingTimeCalculator.class)) {

            mc.when(() -> MeetingConverter.toAlgorithmMeeting(eq(meeting), anyList()))
                    .thenReturn(algoMeeting);
            mc.when(() -> MeetingConverter.toLocalDateTime(bestStartMinutes))
                    .thenReturn(expectedBest);
            mt.when(() -> MeetingTimeCalculator.calculateMeetingTime(eq(algoMeeting)))
                    .thenReturn(bestStartMinutes);

            // when
            service.confirmMeeting(meetingId);

            // then
            // setConfirmedTime / setMeetingStatus 인자 검증
            ArgumentCaptor<LocalDateTime> timeCap = ArgumentCaptor.forClass(LocalDateTime.class);
            verify(meeting).setConfirmedTime(timeCap.capture());
            assertEquals(expectedBest, timeCap.getValue());

            ArgumentCaptor<MeetingStatus> statusCap = ArgumentCaptor.forClass(MeetingStatus.class);
            verify(meeting).setMeetingStatus(statusCap.capture());
            assertEquals(MeetingStatus.BEFORE, statusCap.getValue());

            // 동기화 호출/대안 저장 미호출/저장 호출 검증
            verify(syncService, times(1)).syncMeetingToCalendars(meeting);
            verify(alternativeTimeRepository, never()).saveAll(anyList());
            verify(meetingRepository, times(1)).save(meeting);
        }
    }


    @Test
    @DisplayName("최적 시간이 없고 대안 후보가 있으면 후보 저장 + 메모리 후보로 마감일 설정 + MATCHING (재조회/동기화 없음)")
    void confirmMeeting_whenNoBestTime_andAlternativesExist_setsMatchingAndDeadlineAndSaves() {
        // given
        UUID meetingId = UUID.randomUUID();
        UUID pid = UUID.randomUUID();
        MeetingEntity meeting = mock(MeetingEntity.class);

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(availabilityRepository.findByMeetingId(meetingId))
                .thenReturn(List.of(avail(pid, meetingId, LocalDateTime.now(), LocalDateTime.now().plusHours(1))));

        Meeting algoMeeting = new Meeting("m", List.of(), 0, 60, null, null, List.of(), 0, List.of());

        // 대안 후보 2개(earliest=1/10 → deadline=1/09 23:59)
        int year = LocalDate.now().getYear();
        LocalDateTime s1 = LocalDate.of(year, 1, 10).atTime(9, 0);
        LocalDateTime e1 = s1.plusMinutes(60);
        AlternativeTimeEntity alt1 = AlternativeTimeEntity.builder()
                .alternativeTimeId(UUID.randomUUID())
                .meetingId(meetingId)
                .startTime(s1).endTime(e1)
                .durationAdjustedMinutes(60)
                .excludedParticipants(null)
                .build();

        LocalDateTime s2 = LocalDate.of(year, 1, 12).atTime(14, 0);
        LocalDateTime e2 = s2.plusMinutes(90);
        AlternativeTimeEntity alt2 = AlternativeTimeEntity.builder()
                .alternativeTimeId(UUID.randomUUID())
                .meetingId(meetingId)
                .startTime(s2).endTime(e2)
                .durationAdjustedMinutes(90)
                .excludedParticipants("X,Y")
                .build();

        List<AlternativeTimeEntity> alterTimes = List.of(alt1, alt2);
        LocalDateTime expectedDeadline = LocalDate.of(year, 1, 9).atTime(23, 59);

        try (MockedStatic<MeetingConverter> mc = mockStatic(MeetingConverter.class);
             MockedStatic<MeetingTimeCalculator> mt = mockStatic(MeetingTimeCalculator.class);
             MockedStatic<AlternativeTimeCalculator> ac = mockStatic(AlternativeTimeCalculator.class)) {

            mc.when(() -> MeetingConverter.toAlgorithmMeeting(eq(meeting), anyList()))
                    .thenReturn(algoMeeting);
            mt.when(() -> MeetingTimeCalculator.calculateMeetingTime(eq(algoMeeting)))
                    .thenReturn(null); // 최적 시간 없음
            ac.when(() -> AlternativeTimeCalculator.getAlternativeTimes(eq(algoMeeting), eq(meetingId)))
                    .thenReturn(alterTimes); // 메모리 후보 반환

            // when
            service.confirmMeeting(meetingId);

            // then
            // 후보 저장 호출
            verify(alternativeTimeRepository).saveAll(alterTimes);

            // 마감일 & 상태 인자 검증
            ArgumentCaptor<LocalDateTime> deadlineCap = ArgumentCaptor.forClass(LocalDateTime.class);
            verify(meeting).setAlternativeDeadline(deadlineCap.capture());
            assertEquals(expectedDeadline, deadlineCap.getValue());

            ArgumentCaptor<MeetingStatus> statusCap = ArgumentCaptor.forClass(MeetingStatus.class);
            verify(meeting).setMeetingStatus(statusCap.capture());
            assertEquals(MeetingStatus.MATCHING, statusCap.getValue());

            // 재조회/동기화 미호출
            verify(alternativeTimeRepository, never()).findByMeetingId(any());
            verify(syncService, never()).syncMeetingToCalendars(any());

            // 저장
            verify(meetingRepository).save(meeting);
        }
    }

    @Test
    @DisplayName("최적 시간도 없고 대안 후보도 없으면 MATCH_FAILED (대안 저장/동기화 없음)")
    void confirmMeeting_whenNoBestTime_andNoAlternatives_setsMatchFailed() {
        // given
        UUID meetingId = UUID.randomUUID();
        UUID pid = UUID.randomUUID();
        MeetingEntity meeting = mock(MeetingEntity.class);

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(availabilityRepository.findByMeetingId(meetingId))
                .thenReturn(List.of(avail(pid, meetingId, LocalDateTime.now(), LocalDateTime.now().plusHours(1))));

        Meeting algoMeeting = new Meeting("m", List.of(), 0, 60, null, null, List.of(), 0, List.of());

        try (MockedStatic<MeetingConverter> mc = mockStatic(MeetingConverter.class);
             MockedStatic<MeetingTimeCalculator> mt = mockStatic(MeetingTimeCalculator.class);
             MockedStatic<AlternativeTimeCalculator> ac = mockStatic(AlternativeTimeCalculator.class)) {

            mc.when(() -> MeetingConverter.toAlgorithmMeeting(eq(meeting), anyList()))
                    .thenReturn(algoMeeting);
            mt.when(() -> MeetingTimeCalculator.calculateMeetingTime(eq(algoMeeting)))
                    .thenReturn(null); // 최적 시간 없음
            ac.when(() -> AlternativeTimeCalculator.getAlternativeTimes(eq(algoMeeting), eq(meetingId)))
                    .thenReturn(Collections.emptyList()); // 대안도 없음

            // when
            service.confirmMeeting(meetingId);

            // then
            ArgumentCaptor<MeetingStatus> statusCap = ArgumentCaptor.forClass(MeetingStatus.class);
            verify(meeting).setMeetingStatus(statusCap.capture());
            assertEquals(MeetingStatus.MATCH_FAILED, statusCap.getValue());

            verify(alternativeTimeRepository, never()).saveAll(anyList());
            verify(syncService, never()).syncMeetingToCalendars(any());
            verify(meetingRepository).save(meeting);
        }
    }

    @Test
    @DisplayName("가용 시간이 비어 있으면 INTERNAL_SERVER_ERROR 예외")
    void confirmMeeting_whenNoAvailability_throws() {
        // given
        UUID meetingId = UUID.randomUUID();
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(mock(MeetingEntity.class)));
        when(availabilityRepository.findByMeetingId(meetingId)).thenReturn(Collections.emptyList());

        // when, then
        CustomException ex = assertThrows(CustomException.class,
                () -> service.confirmMeeting(meetingId));
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, ex.getErrorCode()); /// todo 이거 예외처리 종류 바꾸어야할듯

        verify(syncService, never()).syncMeetingToCalendars(any());
        verify(alternativeTimeRepository, never()).saveAll(anyList());
        verify(meetingRepository, never()).save(any());
    }
}
