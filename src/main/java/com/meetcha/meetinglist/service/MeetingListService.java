package com.meetcha.meetinglist.service;

import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.exception.InvalidJoinMeetingRequestException;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;

import com.meetcha.meeting.domain.MeetingStatus;
import com.meetcha.meetinglist.domain.ParticipantEntity;
import com.meetcha.meetinglist.domain.ReflectionStatus;
import com.meetcha.meetinglist.dto.FilteredMeetingResponse;
import com.meetcha.meetinglist.dto.MeetingDetailResponse;
import com.meetcha.meetinglist.dto.MeetingListResponse;
import com.meetcha.meetinglist.dto.ParticipantDto;
import com.meetcha.meetinglist.repository.ParticipantRepository;
import com.meetcha.reflection.domain.MeetingReflectionEntity;
import com.meetcha.reflection.domain.MeetingReflectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingListService {

    private final MeetingRepository meetingRepository;
    private final ParticipantRepository participantRepository;
    private final MeetingReflectionRepository reflectionRepository;

    public MeetingDetailResponse getMeetingDetail(UUID meetingId, String authorizationHeader) {
        // 미팅 상세 조회 로직
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new InvalidJoinMeetingRequestException(ErrorCode.MEETING_NOT_FOUND));

/*        //테스트용
        MeetingEntity meeting = MeetingEntity.builder()
                .meetingId(meetingId)
                .title("테스트 미팅")
                .description("설명 없음")
                .meetingStatus(MeetingStatus.ONGOING)
                .deadline(LocalDateTime.now().plusDays(1))
                .durationMinutes(60)
                .confirmedTime(null)
                .build();

        List<ParticipantDto> participantDtos = List.of(
                new ParticipantDto(UUID.randomUUID(), "테스트유저", "https://example.com/profile.jpg")
        );*/


        // 참여자 조회
        List<ParticipantEntity> participantEntities = participantRepository.findByMeeting_MeetingId(meetingId);

        List<ParticipantDto> participantDtos = participantEntities.stream()
                .map(p -> new ParticipantDto(
                        p.getId(),
                        p.getNickname(),
                        p.getProfileImageUrl()
                ))
                .toList();

/*        return new MeetingDetailResponse(
                UUID.randomUUID(),
                "더미 제목",
                "더미 설명",
                MeetingStatus.ONGOING,
                LocalDateTime.now().plusDays(1),
                60,
                null,
                List.of(new ParticipantDto(UUID.randomUUID(), "더미 유저", null))
        );}}*/

        return new MeetingDetailResponse(
                meeting.getMeetingId(),
                meeting.getTitle(),
                meeting.getDescription(),
                meeting.getMeetingStatus(),
                meeting.getDeadline(),
                meeting.getDurationMinutes(),
                meeting.getConfirmedTime(),
                participantDtos
        );
    }


    public List<MeetingListResponse> getMyMeetings(UUID userId) {
        List<ParticipantEntity> participations = participantRepository.findByUserId(userId);

        return participations.stream()
                .map(ParticipantEntity::getMeeting)
                .map(meeting -> new MeetingListResponse(
                        meeting.getMeetingId(),
                        meeting.getTitle(),
                        meeting.getDeadline(),
                        meeting.getConfirmedTime(),
                        meeting.getDurationMinutes(),
                        meeting.getMeetingStatus()
                ))
                .toList();
    }

    //선택적 미팅 조회 메서드
    public List<FilteredMeetingResponse> getFilteredMeetings(UUID userId, MeetingStatus status, ReflectionStatus reflectionStatus) {
        // 1. 조건에 따라 참여한 미팅 조회
        List<MeetingEntity> meetings = meetingRepository.findAllByParticipant(userId, status);

        // 2. 회고 여부 체크 후 필터링
        return meetings.stream()
                .map(meeting -> {
                    boolean isReflectionWritten = reflectionRepository.existsByMeeting_MeetingId(meeting.getMeetingId());

                    if (reflectionStatus == ReflectionStatus.WRITTEN && !isReflectionWritten) return null;
                    if (reflectionStatus == ReflectionStatus.NOT_WRITTEN && isReflectionWritten) return null;

                    return new FilteredMeetingResponse(
                            meeting.getMeetingId(),
                            meeting.getTitle(),
                            meeting.getDeadline(),
                            meeting.getConfirmedTime(),
                            meeting.getDurationMinutes(),
                            meeting.getMeetingStatus().name(),
                            isReflectionWritten
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }


}

/*    public ParticipantsResponse getParticipants(UUID meetingId, String authorizationHeader) {
        //미팅 참가자 목록 조회 로직 (이거 안해도 될수도)
        return null;
    }
//}*/
