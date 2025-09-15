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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    //특정 사용자가 참여한 프로젝트 목록 조회
    public List<GetProjectsDto> getUserProjects(UUID userId) {
        return projectRepository.findAllByUser_UserId(userId).stream()
                .map(p -> new GetProjectsDto(p.getProjectId(), p.getName())) // name -> projectName
                .toList();
    }

    //프로젝트 생성
    public CreateProjectResponse createProject(CreateProjectRequest request, UUID userId) {
        //유저 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        //중복된 이름 확인
        if (projectRepository.existsByUser_UserIdAndName(userId, request.getName())) {
            throw new CustomException(ErrorCode.DUPLICATE_PROJECT_NAME);
        }

        //프로젝트 테이블에 저장
        ProjectEntity project = ProjectEntity.builder()
                .projectId(UUID.randomUUID())
                .user(user)
                .name(request.getName())
                .createdAt(LocalDateTime.now())
                .build();

        ProjectEntity saved = projectRepository.save(project);

        //응답
        return CreateProjectResponse.builder()
                .projectId(saved.getProjectId())
                .name(saved.getName())
                .createdAt(DateTimeUtils.utcToKst(saved.getCreatedAt()))
                .build();
    }
}