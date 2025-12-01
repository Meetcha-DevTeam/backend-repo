package com.meetcha.meeting.service;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.global.exception.CustomException;
import com.meetcha.meeting.domain.*;
import com.meetcha.meeting.dto.MeetingCreateRequest;
import com.meetcha.meeting.dto.MeetingCreateResponse;
import com.meetcha.project.domain.ProjectEntity;
import com.meetcha.project.domain.ProjectRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.meetcha.global.exception.ErrorCode.INVALID_MEETING_REQUEST;
import static com.meetcha.global.exception.ErrorCode.PROJECT_NOT_FOUND;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {
    @InjectMocks
    MeetingService meetingService;

    @Mock
    ProjectRepository projectRepository;

    @Mock
    MeetingRepository meetingRepository;

    @Mock
    MeetingCandidateDateRepository meetingCandidateDateRepository;

    @DisplayName("올바른 요청이면 createMeeting은 미팅과 후보날짜를 저장한다")
    @Test
    void createMeeting_whenValidRequest_shouldSaveMeetingAndCandidateDates(){
        // given
        UUID projectId = UUID.randomUUID();
        LocalDate candidate1 = LocalDate.now().plusDays(5);
        LocalDate candidate2 = LocalDate.now().plusDays(6);
        LocalDateTime deadline = LocalDateTime.now().plusDays(2);
        MeetingCreateRequest request = new MeetingCreateRequest("title", "desc", 200, List.of(candidate1, candidate2), deadline, projectId);
        UUID userId = UUID.randomUUID();

        ProjectEntity project = new ProjectEntity(projectId, mock(UserEntity.class), "project", LocalDateTime.now());
        Mockito.when(projectRepository.findById(eq(projectId))).thenReturn(Optional.of(project));

        UUID meetingId = UUID.randomUUID();
        when(meetingRepository.save(any(MeetingEntity.class))).thenAnswer(invocation -> {
            // JPA가 PK 세팅해주는 것처럼 흉내
            MeetingEntity arg = invocation.getArgument(0);
            arg.setMeetingId(meetingId);
            return arg;
        });

        // when
        MeetingCreateResponse response = meetingService.createMeeting(request, userId);

        // then
        Assertions.assertThat(response.getMeetingId()).isEqualTo(meetingId);
        verify(projectRepository, times(1)).findById(eq(projectId));
        verify(meetingRepository, times(1)).save(argThat(entity -> {
            Assertions.assertThat(entity.getProject()).isEqualTo(project);
            return true;
        }));

        ArgumentCaptor<List<MeetingCandidateDateEntity>> captor = ArgumentCaptor.forClass((Class) List.class);

        verify(meetingCandidateDateRepository, times(1)).saveAll(captor.capture());

        List<MeetingCandidateDateEntity> savedList = captor.getValue();
        Assertions.assertThat(savedList)
                .hasSize(2)
                .allSatisfy(entity -> Assertions.assertThat(entity.getMeeting().getMeetingId()).isEqualTo(meetingId));
        Assertions.assertThat(savedList)
                .extracting(MeetingCandidateDateEntity::getCandidateDate)
                .containsExactlyInAnyOrderElementsOf(request.getCandidateDates());
    }

    @DisplayName("프로젝트가 존재하지 않으면 createMeeting은 예외를 발생시킨다")
    @Test
    void createMeeting_whenProjectDoesNotExist_shouldThrowException(){
        // given
        UUID projectId = UUID.randomUUID();
        MeetingCreateRequest request = new MeetingCreateRequest("title", "desc", 200, List.of(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6)), LocalDateTime.now().plusDays(2), projectId);
        UUID userId = UUID.randomUUID();

        Mockito.when(projectRepository.findById(eq(projectId))).thenReturn(Optional.empty());

        // when & then
        Assertions.assertThatThrownBy(() -> meetingService.createMeeting(request, userId))
                .isInstanceOf(CustomException.class)
                .hasMessage(PROJECT_NOT_FOUND.getMessage());

        verify(meetingRepository, never()).save(any());
        verify(meetingCandidateDateRepository, never()).saveAll(any());
    }

    @DisplayName("진행시간(분)이 범위를 벗어나면 createMeeting은 예외를 발생시킨다")
    @Test
    void createMeeting_whenDurationMinuteIsOutOfRange_shouldThrowException(){
        // given
        int invalidDurationMinutes = 0;
        UUID projectId = UUID.randomUUID();
        MeetingCreateRequest request = new MeetingCreateRequest("title", "desc", invalidDurationMinutes, List.of(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6)), LocalDateTime.now().plusDays(2), projectId);
        UUID userId = UUID.randomUUID();

        // when & then
        Assertions.assertThatThrownBy(() -> meetingService.createMeeting(request, userId))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_MEETING_REQUEST.getMessage());

        verify(meetingRepository, never()).save(any());
        verify(meetingCandidateDateRepository, never()).saveAll(any());
    }

    @DisplayName("후보날짜가 비어있으면 createMeeting은 예외를 발생시킨다")
    @Test
    void createMeeting_whenCandidateDatesIsEmpty_shouldThrowException(){
        // given
        int invalidDurationMinutes = 0;
        UUID projectId = UUID.randomUUID();
        MeetingCreateRequest request = new MeetingCreateRequest("title", "desc", invalidDurationMinutes, List.of(), LocalDateTime.now().plusDays(2), projectId);
        UUID userId = UUID.randomUUID();

        // when & then
        Assertions.assertThatThrownBy(() -> meetingService.createMeeting(request, userId))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_MEETING_REQUEST.getMessage());

        verify(meetingRepository, never()).save(any());
        verify(meetingCandidateDateRepository, never()).saveAll(any());
    }

    @DisplayName("후보날짜가 최대개수를 넘어서면 createMeeting은 예외를 발생시킨다")
    @Test
    void createMeeting_whenCandidateDatesLengthIsOutOfRange_shouldThrowException(){
        // given
        int invalidDurationMinutes = 0;
        UUID projectId = UUID.randomUUID();
        List<LocalDate> candidateDates = IntStream.rangeClosed(1, 20).mapToObj(i -> LocalDate.now().plusDays(i)).toList();
        MeetingCreateRequest request = new MeetingCreateRequest("title", "desc", invalidDurationMinutes, candidateDates, LocalDateTime.now().plusDays(2), projectId);
        UUID userId = UUID.randomUUID();

        // when & then
        Assertions.assertThatThrownBy(() -> meetingService.createMeeting(request, userId))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_MEETING_REQUEST.getMessage());

        verify(meetingRepository, never()).save(any());
        verify(meetingCandidateDateRepository, never()).saveAll(any());
    }

    @DisplayName("후보날짜가 현재보다 시간상 앞서면 createMeeting은 예외를 발생시킨다")
    @Test
    void createMeeting_whenAnyCandidateDateIsBeforeNow_shouldThrowException(){
        // given
        int invalidDurationMinutes = 0;
        UUID projectId = UUID.randomUUID();
        List<LocalDate> candidateDates = List.of(LocalDate.of(2001, 1, 1));
        MeetingCreateRequest request = new MeetingCreateRequest("title", "desc", invalidDurationMinutes, candidateDates, LocalDateTime.now().plusDays(2), projectId);
        UUID userId = UUID.randomUUID();

        // when & then
        Assertions.assertThatThrownBy(() -> meetingService.createMeeting(request, userId))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_MEETING_REQUEST.getMessage());

        verify(meetingRepository, never()).save(any());
        verify(meetingCandidateDateRepository, never()).saveAll(any());
    }

    @DisplayName("마감날짜가 제일 빠른 후보날짜보다 시간상 앞서지 않으면 createMeeting은 예외를 발생시킨다")
    @Test
    void createMeeting_whenDeadlineIsAfter_shouldThrowException(){
        // given
        int invalidDurationMinutes = 0;
        UUID projectId = UUID.randomUUID();
        LocalDateTime deadline = LocalDateTime.now().plusDays(2);
        List<LocalDate> candidateDates = List.of(deadline.toLocalDate());
        MeetingCreateRequest request = new MeetingCreateRequest("title", "desc", invalidDurationMinutes, candidateDates, deadline, projectId);
        UUID userId = UUID.randomUUID();

        // when & then
        Assertions.assertThatThrownBy(() -> meetingService.createMeeting(request, userId))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_MEETING_REQUEST.getMessage());

        verify(meetingRepository, never()).save(any());
        verify(meetingCandidateDateRepository, never()).saveAll(any());
    }


}