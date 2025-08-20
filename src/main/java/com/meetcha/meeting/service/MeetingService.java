package com.meetcha.meeting.service;

import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.exception.InvalidMeetingRequestException;
import com.meetcha.global.util.DateTimeUtils;
import com.meetcha.meeting.domain.*;
import com.meetcha.meeting.dto.MeetingCreateRequest;
import com.meetcha.meeting.dto.MeetingCreateResponse;
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
        LocalDateTime deadline = DateTimeUtils.kstToUtc(request.deadline());

        // 먼저 meeting 생성(프로젝트는 나중에 세터로)
        MeetingEntity meeting = MeetingEntity.builder()
                .title(request.title())
                .description(request.description())
                .durationMinutes(request.durationMinutes())
                .deadline(deadline)
                .createdAt(now)
                .meetingStatus(MeetingStatus.MATCHING)
                .confirmedTime(null)
                .createdBy(creatorId)
                .meetingCode(UUID.randomUUID().toString().substring(0, 8))
                .build();

        // projectId가 오면 meetings.project_id 세팅
        request.projectId().ifPresent(pid -> {
            ProjectEntity project = projectRepository.findById(pid)
                    .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
            meeting.setProject(project); // ★ 세터로 FK 갱신
        });

        meetingRepository.save(meeting);

        // 후보 날짜 저장
        List<LocalDate> candidateDates = request.candidateDates();
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

    private void validateRequest(MeetingCreateRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (request.durationMinutes() < 1 || request.durationMinutes() > 719) {
            errors.put("durationMinutes", ErrorCode.INVALID_DURATION.getMessage());
        }

        List<LocalDate> dates = request.candidateDates();
        if (dates == null || dates.isEmpty() || dates.size() > 10) {
            errors.put("candidateDates", ErrorCode.INVALID_CANDIDATE_DATES.getMessage());
        } else {
            LocalDate today = LocalDate.now(clock);

            boolean anyPastOrToday = dates.stream().anyMatch(d -> !d.isAfter(today));
            if (anyPastOrToday) {
                errors.put("candidateDates", ErrorCode.INVALID_CANDIDATE_DATE_IN_PAST.getMessage());
            }

            LocalDate earliestCandidate = dates.stream().min(LocalDate::compareTo).orElse(null);
            LocalDateTime deadline = request.deadline();
            if (earliestCandidate != null && deadline.toLocalDate().isAfter(earliestCandidate)) {
                errors.put("deadline", ErrorCode.INVALID_MEETING_DEADLINE.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidMeetingRequestException(errors);
        }
    }
}
