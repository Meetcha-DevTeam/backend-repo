package com.meetcha.project.service;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.util.DateTimeUtils;
import com.meetcha.project.domain.ProjectEntity;
import com.meetcha.project.domain.ProjectRepository;
import com.meetcha.project.dto.CreateProjectRequest;
import com.meetcha.project.dto.CreateProjectResponse;
import com.meetcha.project.dto.GetProjectsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // 특정 사용자가 참여한 프로젝트 목록 조회
    public List<GetProjectsDto> getUserProjects(UUID userId) {
        log.info("[프로젝트 목록 조회 요청] userId={}", userId);

        List<GetProjectsDto> result = projectRepository.findAllByUser_UserId(userId).stream()
                .map(p -> new GetProjectsDto(p.getProjectId(), p.getName()))
                .toList();

        log.info("[프로젝트 목록 조회 성공] userId={}, count={}", userId, result.size());
        return result;
    }

    // 프로젝트 생성
    public CreateProjectResponse createProject(CreateProjectRequest request, UUID userId) {
        log.info("[프로젝트 생성 요청] userId={}, projectName={}", userId, request.getName());

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[프로젝트 생성 실패] 유저를 찾을 수 없음. userId={}", userId);
                    return new CustomException(ErrorCode.USER_NOT_FOUND);
                });

        if (projectRepository.existsByUser_UserIdAndName(userId, request.getName())) {
            log.warn("[프로젝트 생성 실패] 중복된 프로젝트 이름. userId={}, projectName={}",
                    userId, request.getName());
            throw new CustomException(ErrorCode.DUPLICATE_PROJECT_NAME);
        }

        ProjectEntity project = ProjectEntity.builder()
                .projectId(UUID.randomUUID())
                .user(user)
                .name(request.getName())
                .createdAt(LocalDateTime.now())
                .build();

        ProjectEntity saved = projectRepository.save(project);

        log.info("[프로젝트 생성 성공] projectId={}, userId={}, projectName={}",
                saved.getProjectId(), userId, saved.getName());

        return CreateProjectResponse.builder()
                .projectId(saved.getProjectId())
                .name(saved.getName())
                .createdAt(DateTimeUtils.utcToKst(saved.getCreatedAt()))
                .build();
    }
}
