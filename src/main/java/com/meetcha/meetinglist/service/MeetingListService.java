package com.meetcha.meetinglist.service;

import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.exception.InvalidJoinMeetingRequestException;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;

import com.meetcha.meeting.domain.MeetingStatus;
import com.meetcha.meetinglist.domain.ParticipantEntity;
import com.meetcha.meetinglist.dto.FilteredMeetingResponse;

import com.meetcha.meetinglist.dto.MeetingDetailResponse;
import com.meetcha.meetinglist.dto.MeetingListResponse;
import com.meetcha.meetinglist.dto.ParticipantDto;
import com.meetcha.meetinglist.repository.ParticipantRepository;
import com.meetcha.project.domain.UserProjectAliasRepository;
import com.meetcha.project.dto.ProjectSummaryDto;
import com.meetcha.reflection.domain.MeetingReflectionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingListService {

    private final MeetingRepository meetingRepository;
    private final ParticipantRepository participantRepository;
    private final MeetingReflectionRepository reflectionRepository;
    private final UserProjectAliasRepository userProjectAliasRepository;

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

    //작성이 필요한 미팅 조회
    public List<FilteredMeetingResponse> getMeetingsNeedingReflection(UUID userId) {
        //DONE 상태 미팅들 조회
        List<MeetingEntity> meetings = meetingRepository.findByUserIdAndStatus(userId, MeetingStatus.DONE);

        //사용자 프로젝트 목록 조회 (alias or 기본 이름 포함)
        List<ProjectSummaryDto> projectSummaries = userProjectAliasRepository.findProjectsByUserId(userId);

        //Map<projectId, projectName>으로 변환
        Map<UUID, String> projectNameMap = projectSummaries.stream()
                .collect(Collectors.toMap(ProjectSummaryDto::getProjectId, ProjectSummaryDto::getProjectName));

        //회고 미작성 미팅 필터링 + 응답 생성
        return meetings.stream()
                .filter(meeting -> !reflectionRepository.existsByMeeting_MeetingIdAndUser_UserId(meeting.getMeetingId(), userId))
                .map(meeting -> {
                    UUID projectId = meeting.getProjectId();
                    String projectName = projectId != null ? projectNameMap.get(projectId) : null;

                    return new FilteredMeetingResponse(
                            meeting.getMeetingId(),
                            meeting.getTitle(),
                            meeting.getDescription(),
                            projectId,
                            projectName,
                            meeting.getDeadline(),
                            meeting.getConfirmedTime(),
                            meeting.getDurationMinutes(),
                            meeting.getMeetingStatus().name()
                    );
                })
                .toList();
    }

}

/*    public ParticipantsResponse getParticipants(UUID meetingId, String authorizationHeader) {
        //미팅 참가자 목록 조회 로직 (이거 안해도 될수도)
        return null;
    }
//}*/
