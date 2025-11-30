package com.meetcha.meeting.service;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.meeting.domain.MeetingCandidateDateEntity;
import com.meetcha.meeting.domain.MeetingCandidateDateRepository;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.meeting.dto.MeetingCreateRequest;
import com.meetcha.meeting.dto.MeetingCreateResponse;
import com.meetcha.project.domain.ProjectEntity;
import com.meetcha.project.domain.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
        MeetingCreateRequest request = new MeetingCreateRequest("title", "desc", 200, List.of(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6)), LocalDateTime.now().plusDays(2), projectId);
        UUID userId = UUID.randomUUID();

        ProjectEntity project = new ProjectEntity(projectId, mock(UserEntity.class), "project", LocalDateTime.now());
        Mockito.when(projectRepository.findById(eq(projectId))).thenReturn(Optional.of(project));

        // when
        MeetingCreateResponse response = meetingService.createMeeting(request, userId);

        // then
        verify(projectRepository, times(1)).findById(eq(projectId));
        verify(meetingRepository, times(1)).save(argThat(meeting -> {
            assertThat(meeting.getProject()).isNotNull();
            return true;
        }));
        verify(meetingCandidateDateRepository, times(2)).save(any(MeetingCandidateDateEntity.class));
    }

}