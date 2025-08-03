package com.meetcha.reflection.service;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.global.exception.*;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.reflection.domain.MeetingReflectionEntity;
import com.meetcha.reflection.domain.MeetingReflectionRepository;
import com.meetcha.reflection.dto.CreateReflectionRequestDto;
import com.meetcha.reflection.dto.CreateReflectionResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

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
}
