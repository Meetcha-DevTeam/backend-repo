package com.meetcha.meeting.service;

import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.util.DateTimeUtils;
import com.meetcha.joinmeeting.domain.MeetingParticipantRepository;
import com.meetcha.joinmeeting.domain.ParticipantAvailabilityRepository;
import com.meetcha.meeting.domain.*;
import com.meetcha.meeting.dto.MeetingCreateRequest;
import com.meetcha.meeting.dto.MeetingCreateResponse;
import com.meetcha.meeting.dto.MeetingDeleteResponse;
import com.meetcha.meetinglist.repository.AlternativeTimeRepository;
import com.meetcha.meetinglist.repository.AlternativeVoteRepository;
import com.meetcha.project.domain.ProjectEntity;
import com.meetcha.project.domain.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final MeetingCandidateDateRepository candidateDateRepository;
    private final ProjectRepository projectRepository;
    private final MeetingParticipantRepository participantRepository;
    private final ParticipantAvailabilityRepository availabilityRepository;
    private final AlternativeTimeRepository alternativeTimeRepository;
    private final AlternativeVoteRepository alternativeVoteRepository;

    private final Clock clock = Clock.systemDefaultZone();

    @Transactional
    public MeetingCreateResponse createMeeting(MeetingCreateRequest request, UUID creatorId) {
        validateRequest(request);

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime deadline = DateTimeUtils.kstToUtc(request.getDeadline());

        MeetingEntity meeting = createMeetingEntity(request, creatorId, deadline, now);
        MeetingEntity saved = meetingRepository.save(meeting);

        // 후보 날짜 저장
        List<MeetingCandidateDateEntity> candidateDates = request.getCandidateDates().stream()
                .map(date -> new MeetingCandidateDateEntity(saved, date))
                .toList();
        candidateDateRepository.saveAll(candidateDates);

        return new MeetingCreateResponse(
                saved.getMeetingId(),
                DateTimeUtils.utcToKst(saved.getCreatedAt())
        );
    }

    private void validateRequest(MeetingCreateRequest request) {
        Map<String, String> errors = new HashMap<>();

        validateDuration(request, errors);
        validateCandidateDates(request, errors);

        if (!errors.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_MEETING_REQUEST, errors);
        }
    }

    private void validateDuration(MeetingCreateRequest request, Map<String, String> errors) {
        if (request.getDurationMinutes() < 1 || request.getDurationMinutes() > 719) {
            errors.put("durationMinutes", ErrorCode.INVALID_DURATION.getMessage());
        }
    }

    private void validateCandidateDates(MeetingCreateRequest request, Map<String, String> errors) {
        List<LocalDate> dates = request.getCandidateDates();
        if (dates == null || dates.isEmpty() || dates.size() > 10) {
            errors.put("candidateDates", ErrorCode.INVALID_CANDIDATE_DATES.getMessage());
            return;
        }

        LocalDate today = LocalDate.now(clock);
        boolean anyPastOrToday = dates.stream().anyMatch(d -> !d.isAfter(today));
        if (anyPastOrToday) {
            errors.put("candidateDates", ErrorCode.INVALID_CANDIDATE_DATE_IN_PAST.getMessage());
        }

        LocalDate earliestCandidate = dates.stream().min(LocalDate::compareTo).orElse(null);
        LocalDateTime deadline = request.getDeadline();
        if (earliestCandidate != null && deadline.toLocalDate().isAfter(earliestCandidate)) {
            errors.put("deadline", ErrorCode.INVALID_MEETING_DEADLINE.getMessage());
        }
    }

    private MeetingEntity createMeetingEntity(MeetingCreateRequest request, UUID creatorId, LocalDateTime deadline, LocalDateTime now) {
        // 먼저 meeting 생성(프로젝트는 나중에 세터로)
        MeetingEntity meeting = MeetingEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .deadline(deadline)
                .createdAt(now)
                .meetingStatus(MeetingStatus.MATCHING)
                .confirmedTime(null)
                .createdBy(creatorId)
                .meetingCode(UUID.randomUUID().toString().substring(0, 8))
                .build();

        // projectId가 오면 meetings.project_id 세팅
        request.getProjectId().ifPresent(pid -> {
            ProjectEntity project = projectRepository.findById(pid)
                    .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
            meeting.setProject(project); // ★ 세터로 FK 갱신
        });
        return meeting;
    }

    @Transactional
    public MeetingDeleteResponse deleteFailedMeeting(UUID meetingId, UUID userId) {
        // 미팅 조회
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));

        // 미팅 생성자 권한 확인
        if (!meeting.getCreatedBy().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 매칭 실패 상태 확인
        if (meeting.getMeetingStatus() != MeetingStatus.MATCH_FAILED) {
            throw new CustomException(ErrorCode.CANNOT_DELETE_MEETING);
        }

        // 투표 삭제 (대안 시간을 참조하므로 먼저 삭제)
        alternativeVoteRepository.deleteByAlternativeTime_MeetingId(meetingId);

        // 대안 시간 삭제
        alternativeTimeRepository.deleteByMeetingId(meetingId);

        // 참여자 가용 시간 삭제
        availabilityRepository.deleteByMeetingId(meetingId);

        // 참여자 삭제 (미팅을 참조하므로 미팅보다 먼저 삭제)
        participantRepository.deleteByMeeting_MeetingId(meetingId);

        // 미팅 삭제 (cascade로 후보 날짜도 함께 삭제됨)
        meetingRepository.delete(meeting);

        return MeetingDeleteResponse.builder()
                .meetingId(meetingId)
                .message("매칭 실패된 미팅이 삭제되었습니다.")
                .build();
    }
}
