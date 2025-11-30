package com.meetcha.joinmeeting.service;

import com.meetcha.auth.domain.UserRepository;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.util.DateTimeUtils;
import com.meetcha.joinmeeting.domain.MeetingParticipant;
import com.meetcha.joinmeeting.domain.MeetingParticipantRepository;
import com.meetcha.joinmeeting.domain.ParticipantAvailabilityRepository;
import com.meetcha.joinmeeting.dto.JoinMeetingRequest;
import com.meetcha.joinmeeting.dto.JoinMeetingResponse;
import com.meetcha.joinmeeting.dto.ValidateMeetingCodeResponse;
import com.meetcha.meeting.domain.*;
import com.meetcha.meeting.dto.MeetingInfoResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.meetcha.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JoinMeetingServiceTest {
    @InjectMocks
    JoinMeetingService joinMeetingService;

    @Mock
    MeetingRepository meetingRepository;

    @Mock
    MeetingParticipantRepository meetingParticipantRepository;

    @Mock
    ParticipantAvailabilityRepository participantAvailabilityRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    MeetingCandidateDateRepository meetingCandidateDateRepository;

    @DisplayName("유효한 요청이면 join은 참가자 정보와 참여가능시간을 저장한다")
    @Test
    void join_whenValidRequest_saveParticipantAndAvailabilities(){
        // given
        UUID meetingId = UUID.randomUUID();
        String nickname = "nickname";
        JoinMeetingRequest request = new JoinMeetingRequest(nickname, List.of(new JoinMeetingRequest.TimeSlot(
                LocalDateTime.of(2025, 1, 1, 9, 0),
                LocalDateTime.of(2025, 1, 1, 10, 0)
        )));
        UUID userId = UUID.randomUUID();

        MeetingEntity meeting = mock(MeetingEntity.class);
        when(meetingRepository.findById(eq(meetingId))).thenReturn(Optional.of(meeting));
        when(meeting.isDeadlinePassed()).thenReturn(false);

        when(meetingParticipantRepository.existsByMeeting_MeetingIdAndUserId(eq(meetingId), eq(userId))).thenReturn(false);
        UUID participantId = UUID.randomUUID();
        when(meetingParticipantRepository.save(any(MeetingParticipant.class))).thenReturn(new MeetingParticipant(participantId, nickname, userId, meeting));

        // when
        JoinMeetingResponse response = joinMeetingService.join(meetingId, request, userId);

        // then
        Assertions.assertThat(response.getMeetingId()).isEqualTo(meetingId);
        Assertions.assertThat(response.getParticipantId()).isEqualTo(participantId);
        verify(meetingParticipantRepository, times(1)).save(any(MeetingParticipant.class));
        verify(participantAvailabilityRepository, times(1)).saveAll(argThat(list -> {
            Assertions.assertThat(list).hasSize(1);
            Assertions.assertThat(list.iterator().next().getParticipantId()).isEqualTo(participantId);
            Assertions.assertThat(list.iterator().next().getMeetingId()).isEqualTo(meetingId);
            return true;
        }));
    }

    @DisplayName("미팅이 존재하지 않으면 join은 예외를 발생시킨다")
    @Test
    void join_whenMeetingDoesNotExist_shouldThrowException(){
        // given
        UUID meetingId = UUID.randomUUID();
        String nickname = "nickname";
        JoinMeetingRequest request = new JoinMeetingRequest(nickname, List.of(new JoinMeetingRequest.TimeSlot(
                LocalDateTime.of(2025, 1, 1, 9, 0),

                LocalDateTime.of(2025, 1, 1, 10, 0)

        )));
        UUID userId = UUID.randomUUID();

        MeetingEntity meeting = mock(MeetingEntity.class);
        when(meetingRepository.findById(eq(meetingId))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> joinMeetingService.join(meetingId, request, userId))
                .isInstanceOf(CustomException.class)
                .hasMessage(MEETING_NOT_FOUND.getMessage());
    }

    @DisplayName("마감 시간이 지났다면 join은 예외를 발생시킨다")
    @Test
    void join_whenDeadlineIsPassed_shouldThrowException(){
        // given
        UUID meetingId = UUID.randomUUID();
        String nickname = "nickname";
        JoinMeetingRequest request = new JoinMeetingRequest(nickname, List.of(new JoinMeetingRequest.TimeSlot(
                LocalDateTime.of(2025, 1, 1, 9, 0),

                LocalDateTime.of(2025, 1, 1, 10, 0)

        )));
        UUID userId = UUID.randomUUID();

        MeetingEntity meeting = mock(MeetingEntity.class);
        when(meetingRepository.findById(eq(meetingId))).thenReturn(Optional.of(meeting));
        when(meeting.isDeadlinePassed()).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> joinMeetingService.join(meetingId, request, userId))
                .isInstanceOf(CustomException.class)
                .hasMessage(MEETING_DEADLINE_PASSED.getMessage());
    }

    @DisplayName("중복 요청이라면 join은 예외를 발생시킨다")
    @Test
    void join_whenDuplicateRequest_shouldThrowException(){
        // given
        UUID meetingId = UUID.randomUUID();
        String nickname = "nickname";
        JoinMeetingRequest request = new JoinMeetingRequest(nickname, List.of(new JoinMeetingRequest.TimeSlot(
                LocalDateTime.of(2025, 1, 1, 9, 0),

                LocalDateTime.of(2025, 1, 1, 10, 0)

        )));
        UUID userId = UUID.randomUUID();

        MeetingEntity meeting = mock(MeetingEntity.class);
        when(meetingRepository.findById(eq(meetingId))).thenReturn(Optional.of(meeting));
        when(meeting.isDeadlinePassed()).thenReturn(false);

        when(meetingParticipantRepository.existsByMeeting_MeetingIdAndUserId(eq(meetingId), eq(userId))).thenReturn(true);


        // when & then
        assertThatThrownBy(() -> joinMeetingService.join(meetingId, request, userId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ALREADY_JOINED_MEETING.getMessage());
    }


    @DisplayName("타임슬롯이 유효하지 않다면 join은 예외를 발생시킨다")
    @Test
    void join_whenInvalidTimeSlot_shouldThrowException(){
        // given
        UUID meetingId = UUID.randomUUID();
        String nickname = "nickname";
        JoinMeetingRequest request = new JoinMeetingRequest(nickname, List.of(new JoinMeetingRequest.TimeSlot(
                LocalDateTime.of(2025, 1, 1, 10, 0),

                LocalDateTime.of(2025, 1, 1, 9, 0)

        )));
        UUID userId = UUID.randomUUID();

        MeetingEntity meeting = mock(MeetingEntity.class);
        when(meetingRepository.findById(eq(meetingId))).thenReturn(Optional.of(meeting));
        when(meeting.isDeadlinePassed()).thenReturn(false);

        when(meetingParticipantRepository.existsByMeeting_MeetingIdAndUserId(eq(meetingId), eq(userId))).thenReturn(false);


        // when & then
        assertThatThrownBy(() -> joinMeetingService.join(meetingId, request, userId))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_TIME_SLOT.getMessage());
    }

    @DisplayName("요청에 닉네임이 없는데 해당하는 유저가 존재하지 않으면 join은 예외를 발생시킨다")
    @Test
    void join_whenNicknameIsEmptyAndUserDoesNotExists_shouldThrowException(){
        // given
        UUID meetingId = UUID.randomUUID();
        String nickname = null;
        JoinMeetingRequest request = new JoinMeetingRequest(nickname, List.of(new JoinMeetingRequest.TimeSlot(
                LocalDateTime.of(2025, 1, 1, 9, 0),
                LocalDateTime.of(2025, 1, 1, 10, 0)
        )));
        UUID userId = UUID.randomUUID();

        MeetingEntity meeting = mock(MeetingEntity.class);
        when(meetingRepository.findById(eq(meetingId))).thenReturn(Optional.of(meeting));
        when(meeting.isDeadlinePassed()).thenReturn(false);

        when(userRepository.findById(eq(userId))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> joinMeetingService.join(meetingId, request, userId))
                .isInstanceOf(CustomException.class)
                .hasMessage(USER_NOT_FOUND.getMessage());
    }

    @DisplayName("올바른 요청이면 getMeetingInfo는 미팅 정보를 반환한다")
    @Test
    void getMeetingInfo_whenValidRequest_shouldReturnMeetingInfo(){
        // given
        UUID meetingId = UUID.randomUUID();
        String meetingCode = UUID.randomUUID().toString().substring(0, 8);
        MeetingEntity meeting = new MeetingEntity(meetingId, "title", "desc", 100, LocalDateTime.now().plusDays(1), LocalDateTime.now(), MeetingStatus.MATCHING, null, meetingCode, null, UUID.randomUUID(), null, null);
        when(meetingRepository.findById(eq(meetingId))).thenReturn(Optional.of(meeting));

        UUID cadidateDateId = UUID.randomUUID();
        MeetingCandidateDateEntity meetingCandidateDateEntity = new MeetingCandidateDateEntity(cadidateDateId, LocalDate.now().plusDays(4), meeting);
        when(meetingCandidateDateRepository.findAllByMeeting_MeetingId(eq(meetingId))).thenReturn(List.of(meetingCandidateDateEntity));

        // when
        MeetingInfoResponse meetingInfo = joinMeetingService.getMeetingInfo(meetingId);

        // then
        Assertions.assertThat(meetingInfo.getMeetingId()).isEqualTo(meetingId);
        Assertions.assertThat(meetingInfo.getCandidateDates()).containsExactly(meetingCandidateDateEntity.getCandidateDate());
        Assertions.assertThat(meetingInfo.getDeadline()).isEqualTo( DateTimeUtils.utcToKst(meeting.getDeadline()));
    }

    @DisplayName("미팅이 존재하지 않으면 getMeetingInfo는 예외를 발생시킨다")
    @Test
    void getMeetingInfo_whenMeetingDoesNotExist_shouldThrowException(){
        // given
        UUID meetingId = UUID.randomUUID();
        when(meetingRepository.findById(eq(meetingId))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> joinMeetingService.getMeetingInfo(meetingId))
                .isInstanceOf(CustomException.class)
                .hasMessage(MEETING_NOT_FOUND.getMessage());

    }

    @DisplayName("마감시간이 지났다면 validateMeetingCode는 closed라고 응답한다")
    @Test
    void validateMeetingCode_whenDeadlineIsPassed_shouldReturnClosedAsTrue(){
        // given
        UUID meetingId = UUID.randomUUID();
        String meetingCode = UUID.randomUUID().toString().substring(0, 8);
        MeetingEntity meeting = new MeetingEntity(meetingId, "title", "desc", 100, LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(2), MeetingStatus.MATCHING, null, meetingCode, null, UUID.randomUUID(), null, null);
        when(meetingRepository.findByMeetingCode(eq(meetingCode))).thenReturn(Optional.of(meeting));

        // when
        ValidateMeetingCodeResponse response = joinMeetingService.validateMeetingCode(meetingCode);

        // then
        assertThat(response.getMeetingId()).isEqualTo(meetingId);
        assertThat(response.getIsClosed()).isEqualTo(true);
    }

    @DisplayName("validateMeetingCode는 closed라고 응답한다")
    @Test
    void validateMeetingCode_whenMeetingDoesNotExist_shouldThrowException(){
        // given
        String meetingCode = UUID.randomUUID().toString().substring(0, 8);
        when(meetingRepository.findByMeetingCode(eq(meetingCode))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> joinMeetingService.validateMeetingCode(meetingCode))
                .isInstanceOf(CustomException.class)
                .hasMessage(MEETING_NOT_FOUND.getMessage());
    }


}