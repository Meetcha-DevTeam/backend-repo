package com.meetcha.meetinglist.service;

import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.util.DateTimeUtils;
import com.meetcha.joinmeeting.domain.MeetingParticipantRepository;
import com.meetcha.joinmeeting.dto.MeetingParticipantDto;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.meeting.domain.MeetingStatus;
import com.meetcha.meetinglist.dto.NeedReflectionResponse;
import com.meetcha.meetinglist.dto.MeetingDetailResponse;
import com.meetcha.meetinglist.dto.MeetingListResponse;
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
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MeetingReflectionRepository reflectionRepository;

    public MeetingDetailResponse getMeetingDetail(UUID meetingId, String authorizationHeader) {
        long startNs = System.nanoTime();
        log.info("[MEETING_DETAIL] start meetingId={}", meetingId);

        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> {
                    log.warn("[MEETING_DETAIL] meeting not found meetingId={}", meetingId);
                    return new CustomException(ErrorCode.MEETING_NOT_FOUND);
                });

        List<MeetingParticipantDto> participantDtos =
                meetingParticipantRepository.findParticipantDtosByMeetingId(meetingId);

        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        log.info("[MEETING_DETAIL] success meetingId={} status={} participants={} elapsedMs={}",
                meetingId, meeting.getMeetingStatus(), participantDtos.size(), elapsedMs);

        return new MeetingDetailResponse(
                meeting.getMeetingId(),
                meeting.getTitle(),
                meeting.getDescription(),
                meeting.getMeetingStatus(),
                DateTimeUtils.utcToKst(meeting.getDeadline()),
                meeting.getDurationMinutes(),
                DateTimeUtils.utcToKst(meeting.getConfirmedTime()),
                meeting.getMeetingCode(),
                participantDtos
        );
    }

    @Transactional(readOnly = true)
    public List<MeetingListResponse> getMyMeetings(UUID userId) {
        long startNs = System.nanoTime();
        log.info("[MEETING_LIST] start userId={}", userId);

        List<MeetingEntity> meetings = meetingRepository.findMyMeetings(userId);

        List<MeetingListResponse> result = meetings.stream()
                .map(m -> new MeetingListResponse(
                        m.getMeetingId(),
                        m.getTitle(),
                        m.getDeadline(),
                        m.getConfirmedTime(),
                        m.getDurationMinutes(),
                        m.getMeetingStatus()
                ))
                .toList();

        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        log.info("[MEETING_LIST] success userId={} count={} elapsedMs={}",
                userId, result.size(), elapsedMs);

        return result;
    }

    // 작성이 필요한 미팅 조회
    @Transactional(readOnly = true)
    public List<NeedReflectionResponse> getMeetingsNeedingReflection(UUID userId) {
        long startNs = System.nanoTime();
        log.info("[NEED_REFLECTION] start userId={}", userId);

        List<MeetingEntity> meetings = meetingRepository.getMeetingsNeedReflection(userId, MeetingStatus.DONE);

        List<NeedReflectionResponse> result = meetings.stream()
                .filter(m -> !reflectionRepository.existsByMeeting_MeetingIdAndUser_UserId(m.getMeetingId(), userId))
                .map(m -> {
                    var project = m.getProject();
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

        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        log.info("[NEED_REFLECTION] success userId={} fetched={} result={} elapsedMs={}",
                userId, meetings.size(), result.size(), elapsedMs);

        return result;
    }
}
