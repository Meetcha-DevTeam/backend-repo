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
import com.meetcha.reflection.domain.MeetingReflectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingListService {

    private final MeetingRepository meetingRepository;
    private final ParticipantRepository participantRepository;
    private final MeetingReflectionRepository reflectionRepository;

    public MeetingDetailResponse getMeetingDetail(UUID meetingId, String authorizationHeader) {
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new InvalidJoinMeetingRequestException(ErrorCode.MEETING_NOT_FOUND));

        List<ParticipantEntity> participantEntities = participantRepository.findByMeeting_MeetingId(meetingId);

        List<ParticipantDto> participantDtos = participantEntities.stream()
                .map(p -> new ParticipantDto(
                        p.getId(),
                        p.getNickname(),
                        p.getProfileImageUrl()
                ))
                .toList();

        return new MeetingDetailResponse(
                meeting.getMeetingId(),
                meeting.getTitle(),
                meeting.getDescription(),
                meeting.getMeetingStatus(),
                meeting.getDeadline(),
                meeting.getDurationMinutes(),
                meeting.getConfirmedTime(),
                meeting.getMeetingCode(),
                participantDtos
        );
    }

    @Transactional(readOnly = true)
    public List<MeetingListResponse> getMyMeetings(UUID userId) {
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

    // 작성이 필요한 미팅 조회
    @Transactional(readOnly = true)
    public List<NeedReflectionResponse> getMeetingsNeedingReflection(UUID userId) {
        List<MeetingEntity> meetings = meetingRepository.getMeetingsNeedReflection(userId, MeetingStatus.DONE);

        return meetings.stream()
                .filter(m -> !reflectionRepository.existsByMeeting_MeetingIdAndUser_UserId(m.getMeetingId(), userId))
                .map(m -> {
                    var project = m.getProject(); // ManyToOne ProjectEntity (nullable 가능)
                    UUID projectId = project != null ? project.getProjectId() : null;
                    String projectName = project != null ? project.getName() : null;

                    return new NeedReflectionResponse(
                            m.getMeetingId(),
                            m.getTitle(),
                            m.getDescription(),
                            projectId,
                            projectName,
                            m.getConfirmedTime(),
                            m.getMeetingStatus().name()
                    );
                })
                .toList();
    }
}
