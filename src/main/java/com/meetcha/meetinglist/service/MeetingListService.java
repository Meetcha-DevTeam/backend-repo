package com.meetcha.meetinglist.service;

import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.exception.InvalidJoinMeetingRequestException;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;

import com.meetcha.meeting.domain.MeetingStatus;
import com.meetcha.meetinglist.domain.ParticipantEntity;
import com.meetcha.meetinglist.dto.NeedReflectionResponse;

import com.meetcha.meetinglist.dto.MeetingDetailResponse;
import com.meetcha.meetinglist.dto.MeetingListResponse;
import com.meetcha.meetinglist.dto.ParticipantDto;
import com.meetcha.meetinglist.repository.ParticipantRepository;
import com.meetcha.project.domain.UserProjectAliasRepository;
import com.meetcha.project.dto.GetProjectsDto;
import com.meetcha.reflection.domain.MeetingReflectionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
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


    @Transactional(readOnly = true)
    public List<MeetingListResponse> getMyMeetings(UUID userId) {
        // 생성자 or 참가자인 미팅 전부 (중복 제거, 최신순)
        List<MeetingEntity> meetings = meetingRepository.findMyMeetings(userId);

        return meetings.stream()
                .map(m -> new MeetingListResponse(
                        m.getMeetingId(),
                        m.getTitle(),
                        m.getDeadline(),
                        m.getConfirmedTime(),
                        m.getDurationMinutes(),
                        m.getMeetingStatus()
                ))
                .toList();
    }



    //작성이 필요한 미팅 조회
    public List<NeedReflectionResponse> getMeetingsNeedingReflection(UUID userId) {
        //DONE 상태 미팅들 조회
        List<MeetingEntity> meetings = meetingRepository.findByUserIdAndStatus(userId, MeetingStatus.DONE);

        //사용자 프로젝트 목록 조회 (alias or 기본 이름 포함)
        List<GetProjectsDto> projectSummaries = userProjectAliasRepository.findProjectsByUserId(userId);

        //Map<projectId, projectName>으로 변환
        Map<UUID, String> projectNameMap = projectSummaries.stream()
                .collect(Collectors.toMap(GetProjectsDto::getProjectId, GetProjectsDto::getProjectName));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return meetings.stream()
                .filter(meeting -> !reflectionRepository.existsByMeeting_MeetingIdAndUser_UserId(meeting.getMeetingId(), userId))
                .map(meeting -> {
                    UUID projectId = meeting.getProjectId();
                    String projectName = projectId != null ? projectNameMap.get(projectId) : null;

                    String formattedTime = meeting.getConfirmedTime() != null
                            ? meeting.getConfirmedTime().format(formatter)
                            : null;

                    return new NeedReflectionResponse(
                            meeting.getMeetingId(),
                            meeting.getTitle(),
                            meeting.getDescription(),
                            projectId,
                            projectName,
                            formattedTime, //String으로 포맷팅
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
