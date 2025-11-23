package com.meetcha.meeting.service;

import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.util.DateTimeUtils;
import com.meetcha.meeting.domain.*;
import com.meetcha.meeting.dto.MeetingCreateRequest;
import com.meetcha.meeting.dto.MeetingCreateResponse;
import com.meetcha.meeting.dto.MeetingDeleteResponse;
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

    private final Clock clock = Clock.systemDefaultZone();

    @Transactional
    public MeetingCreateResponse createMeeting(MeetingCreateRequest request, UUID creatorId) {
        validateRequest(request);

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime deadline = DateTimeUtils.kstToUtc(request.getDeadline());

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

        meetingRepository.save(meeting);

        // 후보 날짜 저장
        List<LocalDate> candidateDates = request.getCandidateDates();
        if (candidateDates != null && !candidateDates.isEmpty()) {
            for (LocalDate date : candidateDates) {
                candidateDateRepository.save(new MeetingCandidateDateEntity(meeting, date));
            }
        }

        return new MeetingCreateResponse(
                meeting.getMeetingId(),
                DateTimeUtils.utcToKst(meeting.getCreatedAt())
        );
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

        // 미팅 삭제 (cascade로 후보 날짜도 함께 삭제됨)
        meetingRepository.delete(meeting);

        return MeetingDeleteResponse.builder()
                .meetingId(meetingId)
                .message("매칭 실패된 미팅이 삭제되었습니다.")
                .build();
    }

    private void validateRequest(MeetingCreateRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (request.getDurationMinutes() < 1 || request.getDurationMinutes() > 719) {
            errors.put("durationMinutes", ErrorCode.INVALID_DURATION.getMessage());
        }

        List<LocalDate> dates = request.getCandidateDates();
        if (dates == null || dates.isEmpty() || dates.size() > 10) {
            errors.put("candidateDates", ErrorCode.INVALID_CANDIDATE_DATES.getMessage());
        } else {
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

        if (!errors.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_MEETING_REQUEST, errors);
        }
    }
}
