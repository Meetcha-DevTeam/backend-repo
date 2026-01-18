package com.meetcha.reflection.service;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.global.exception.*;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.meeting.domain.MeetingStatus;
import com.meetcha.project.domain.ProjectEntity;
import com.meetcha.reflection.domain.MeetingReflectionEntity;
import com.meetcha.reflection.domain.MeetingReflectionRepository;
import com.meetcha.reflection.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingReflectionService {

    private final MeetingReflectionRepository reflectionRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final com.meetcha.project.domain.ProjectRepository projectRepository;

    @Transactional
    public CreateReflectionResponseDto createReflection(UUID userId, UUID meetingId, CreateReflectionRequestDto dto) {
        log.info("[회고 생성 요청] userId={}, meetingId={}", userId, meetingId);

        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> {
                    log.warn("[회고 생성 실패] 미팅을 찾을 수 없음. meetingId={}", meetingId);
                    return new CustomException(ErrorCode.MEETING_NOT_FOUND);
                });

        if (meeting.getMeetingStatus() != MeetingStatus.DONE) {
            log.warn("[회고 생성 실패] 회고 작성이 허용되지 않은 미팅 상태. meetingId={}, status={}",
                    meetingId, meeting.getMeetingStatus());
            throw new CustomException(ErrorCode.REFLECTION_NOT_ALLOWED_FOR_MEETING_STATUS);
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[회고 생성 실패] 유저를 찾을 수 없음. userId={}", userId);
                    return new CustomException(ErrorCode.USER_NOT_FOUND);
                });

        if (reflectionRepository.existsByMeeting_MeetingIdAndUser_UserId(meetingId, userId)) {
            log.warn("[회고 생성 실패] 이미 회고가 작성된 미팅. userId={}, meetingId={}", userId, meetingId);
            throw new CustomException(ErrorCode.ALREADY_SUBMITTED_REFLECTION);
        }

        if (dto.getProjectId() != null) {
            ProjectEntity project = projectRepository.findById(dto.getProjectId())
                    .orElseThrow(() -> {
                        log.warn("[회고 생성 실패] 프로젝트를 찾을 수 없음. projectId={}", dto.getProjectId());
                        return new CustomException(ErrorCode.PROJECT_NOT_FOUND);
                    });
            meeting.setProject(project);
        }

        MeetingReflectionEntity reflection = MeetingReflectionEntity.builder()
                .meeting(meeting)
                .user(user)
                .contribution(dto.getContribution())
                .role(dto.getRole())
                .thought(dto.getThought())
                .completedWork(dto.getCompletedWork())
                .plannedWork(dto.getPlannedWork())
                .createdAt(LocalDateTime.now())
                .build();

        reflectionRepository.save(reflection);

        log.info("[회고 생성 성공] reflectionId={}, userId={}, meetingId={}",
                reflection.getReflectionId(), userId, meetingId);

        return new CreateReflectionResponseDto(reflection.getReflectionId());
    }

    // 미팅 회고 목록 요약 조회
    @Transactional(readOnly = true)
    public List<GetWrittenReflectionResponse> getWrittenReflections(UUID userId) {
        log.info("[회고 목록 조회 요청] userId={}", userId);

        List<GetWrittenReflectionResponse> result =
                reflectionRepository.findWrittenReflectionByUserId(userId);

        log.info("[회고 목록 조회 성공] userId={}, count={}", userId, result.size());
        return result;
    }

    // 특정 회고 상세 조회
    @Transactional(readOnly = true)
    public GetReflectionResponse getReflectionDetail(UUID userId, UUID meetingId) {
        log.info("[회고 상세 조회 요청] userId={}, meetingId={}", userId, meetingId);

        GetReflectionResponse response =
                reflectionRepository.findReflectionDetailByMeetingIdAndUserId(meetingId, userId)
                        .orElseThrow(() -> {
                            log.warn("[회고 상세 조회 실패] 회고를 찾을 수 없음. userId={}, meetingId={}",
                                    userId, meetingId);
                            return new CustomException(ErrorCode.REFLECTION_NOT_FOUND);
                        });

        log.info("[회고 상세 조회 성공] userId={}, meetingId={}", userId, meetingId);
        return response;
    }

    // 미팅 건수 + 기여도 + 역할 조회
    @Transactional(readOnly = true)
    public GetReflectionSummaryResponse getReflectionSummary(UUID userId) {
        log.info("[회고 요약 조회 요청] userId={}", userId);

        List<MeetingReflectionEntity> reflections = reflectionRepository.findAllByUserId(userId);
        int writtenCount = reflections.size();

        double averageContribution = reflections.stream()
                .mapToInt(MeetingReflectionEntity::getContribution)
                .average()
                .orElse(0);

        String mostFrequentRole = reflections.stream()
                .map(MeetingReflectionEntity::getRole)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted((a, b) -> {
                    int cmp = Long.compare(b.getValue(), a.getValue());
                    if (cmp != 0) return cmp;
                    return Integer.compare(lastIndexOf(reflections, a.getKey()), lastIndexOf(reflections, b.getKey()));
                })
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        long unwrittenCount = meetingRepository.countMeetingsNeedReflection(userId, MeetingStatus.DONE);
        int totalReflections = writtenCount + (int) unwrittenCount;

        log.info("[회고 요약 조회 성공] userId={}, total={}, avgContribution={}, role={}",
                userId, totalReflections, Math.round(averageContribution), mostFrequentRole);

        return new GetReflectionSummaryResponse(
                totalReflections,
                (int) Math.round(averageContribution),
                mostFrequentRole
        );
    }

    private int lastIndexOf(List<MeetingReflectionEntity> list, String role) {
        for (int i = list.size() - 1; i >= 0; i--) {
            if (role.equals(list.get(i).getRole())) return i;
        }
        return -1;
    }
}
