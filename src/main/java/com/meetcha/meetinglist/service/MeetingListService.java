package com.meetcha.meetinglist.service;

import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.util.DateTimeUtils;
import com.meetcha.joinmeeting.domain.MeetingParticipantRepository;
import com.meetcha.joinmeeting.domain.ParticipantAvailability;
import com.meetcha.joinmeeting.domain.ParticipantAvailabilityRepository;
import com.meetcha.joinmeeting.dto.MeetingParticipantDto;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.meeting.domain.MeetingStatus;
import com.meetcha.meetinglist.dto.NeedReflectionResponse;
import com.meetcha.meetinglist.dto.MeetingDetailResponse;
import com.meetcha.meetinglist.dto.MeetingListResponse;
import com.meetcha.meetinglist.dto.MeetingAllAvailabilitiesResponse;
import com.meetcha.reflection.domain.MeetingReflectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingListService {

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MeetingReflectionRepository reflectionRepository;
    private final ParticipantAvailabilityRepository participantAvailabilityRepository;

    public MeetingDetailResponse getMeetingDetail(UUID meetingId, UUID userId) {
        long startNs = System.nanoTime();
        log.info("[MEETING_DETAIL] start meetingId={}", meetingId);

        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> {
                    log.warn("[MEETING_DETAIL] meeting not found meetingId={}", meetingId);
                    return new CustomException(ErrorCode.MEETING_NOT_FOUND);
                });

        //userID에 해당하는 meetingID 인지 검증하는 로직
        // 미팅이 존재하는지 부터 검증 한 후, 자격을 검증
        boolean isParticipant = meetingParticipantRepository.existsByMeeting_MeetingIdAndUserId(meetingId, userId);
        if (!isParticipant) {
            log.warn("[MEETING_DETAIL] forbidden meetingId={} userId={}", meetingId, userId);
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

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

    @Transactional(readOnly = true)
    public MeetingAllAvailabilitiesResponse getAllParticipantsAvailabilities(UUID meetingId) {

        // 0) 미팅 존재 확인
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));

        // 1) 미팅 참여자 participantId 전부 조회
        List<UUID> participantIds = meetingParticipantRepository.findParticipantIdsByMeetingId(meetingId);

        if (participantIds.isEmpty()) {
            return MeetingAllAvailabilitiesResponse.builder()
                    .participants(List.of())
                    .count(0)
                    .build();
        }

        // 2) 미팅의 모든 참가 가능 시간 조회
        List<ParticipantAvailability> availEntities =
                participantAvailabilityRepository.findByMeetingId(meetingId);

        // 3) participantId 기준 그룹핑
        Map<UUID, List<MeetingAllAvailabilitiesResponse.Availability>> availMap =
                availEntities.stream()
                        .collect(Collectors.groupingBy(
                                ParticipantAvailability::getParticipantId,
                                Collectors.mapping(a -> MeetingAllAvailabilitiesResponse.Availability.builder()
                                        .availabilityId(a.getAvailabilityId())
                                        .startAt(DateTimeUtils.utcToKst(a.getStartAt()))
                                        .endAt(DateTimeUtils.utcToKst(a.getEndAt()))
                                        .build(), Collectors.toList())
                        ));

        // 4) 모든 participantId에 대해 응답 조립 (없으면 빈 배열)
        List<MeetingAllAvailabilitiesResponse.ParticipantAvailabilities> participants =
                participantIds.stream()
                        .map(pid -> MeetingAllAvailabilitiesResponse.ParticipantAvailabilities.builder()
                                .participantId(pid)
                                .availabilities(availMap.getOrDefault(pid, List.of()))
                                .build())
                        .toList();

        return MeetingAllAvailabilitiesResponse.builder()
                .participants(participants)
                .count(participants.size())
                .build();
    }
}
