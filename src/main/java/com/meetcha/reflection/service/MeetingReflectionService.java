package com.meetcha.reflection.service;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.global.exception.*;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.reflection.domain.MeetingReflectionEntity;
import com.meetcha.reflection.domain.MeetingReflectionRepository;
import com.meetcha.reflection.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingReflectionService {

    private final MeetingReflectionRepository reflectionRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreateReflectionResponseDto createReflection(UUID userId, UUID meetingId, CreateReflectionRequestDto dto) {

        // 미팅 존재 여부 확인
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEETING_NOT_FOUND));

        // 사용자 존재 여부 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.USER_NOT_FOUND));

        // 중복 회고 작성 방지
        if (reflectionRepository.findByMeeting_MeetingIdAndUser_UserId(meetingId, userId).isPresent()) {
            throw new ConflictException(ErrorCode.ALREADY_SUBMITTED_REFLECTION);
        }

        // 회고 저장
        MeetingReflectionEntity reflection = MeetingReflectionEntity.builder()
                .meeting(meeting)
                .user(user)
                .projectId(dto.getProjectId()) // nullable
                .contribution(dto.getContribution())
                .role(dto.getRole())
                .thought(dto.getThought())
                .completedWork(dto.getCompletedWork())   // nullable
                .plannedWork(dto.getPlannedWork())       // nullable
                .createdAt(LocalDateTime.now())
                .build();

        reflectionRepository.save(reflection);

        return new CreateReflectionResponseDto(reflection.getReflectionId());
    }

    //미팅 회고 목록 요약 조회
    @Transactional(readOnly = true)
    public List<GetWrittenReflectionResponse> getWrittenReflections(UUID userId) {
        return reflectionRepository.findWrittenReflectionByUserId(userId);
    }

    //특정 회고 상세 조회
    public GetReflectionResponse getReflectionDetail(UUID userId, UUID meetingId) {
        return reflectionRepository.findReflectionDetailByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REFLECTION_NOT_FOUND));
    }

    //미팅건수+기여도+역할조회
    @Transactional(readOnly = true)
    public GetReflectionSummaryResponse getReflectionSummary(UUID userId) {
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

        long unwrittenCount = meetingRepository.countMeetingsNeedReflection(userId);

        int totalReflections = writtenCount + (int) unwrittenCount;

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
