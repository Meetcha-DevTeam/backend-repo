package com.meetcha.meeting.service;

import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.exception.InvalidMeetingRequestException;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.meeting.domain.MeetingStatus;
import com.meetcha.meeting.dto.MeetingCreateRequest;
import com.meetcha.meeting.dto.MeetingCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final Clock clock = Clock.systemDefaultZone();

    public MeetingCreateResponse createMeeting(MeetingCreateRequest request, UUID creatorId) {
        validateRequest(request);

        LocalDateTime now = LocalDateTime.now(clock);

        MeetingEntity meeting = MeetingEntity.builder()
                .title(request.title())
                .description(request.description())
                .durationMinutes(request.durationMinutes())
                .deadline(request.deadline())
                .createdAt(now)
                .meetingStatus(MeetingStatus.BEFORE)
                .confirmedTime(null)
                .createdBy(creatorId)
                .projectId(request.projectId().orElse(null))
                .code(UUID.randomUUID().toString().substring(0, 8))
                .build();

        meetingRepository.save(meeting);

        return new MeetingCreateResponse(meeting.getMeetingId(), meeting.getCreatedAt());
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
                errors.put("candidateDates", ErrorCode.CANDIDATE_DATE_IN_PAST.getMessage());
            }

            LocalDate earliestCandidate = dates.stream().min(LocalDate::compareTo).orElse(null);
            if (earliestCandidate != null &&
                    request.deadline().toLocalDate().isAfter(earliestCandidate)) {
                errors.put("deadline", ErrorCode.DEADLINE_AFTER_CANDIDATE.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidMeetingRequestException(errors);
        }
    }
}
