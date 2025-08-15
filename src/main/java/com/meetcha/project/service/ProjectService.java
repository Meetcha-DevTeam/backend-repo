package com.meetcha.project.service;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.global.exception.ConflictException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.exception.NotFoundException;
import com.meetcha.project.domain.ProjectEntity;
import com.meetcha.project.domain.ProjectRepository;
import com.meetcha.project.domain.UserProjectAliasEntity;
import com.meetcha.project.domain.UserProjectAliasRepository;
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
    //특정 사용자가 참여한 프로젝트 목록 조회
    private final UserProjectAliasRepository aliasRepository;

    public List<GetProjectsDto> getUserProjects(UUID userId) {
        return aliasRepository.findProjectsByUserId(userId);
    }


    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    //프로젝트 생성
    public CreateProjectResponse createProject(CreateProjectRequest request, UUID userId){
        //중복된 이름 확인
        if (projectRepository.findByName(request.getName()).isPresent()) {
            throw new ConflictException(ErrorCode.DUPLICATE_PROJECT_NAME);
        }

        //프로젝트 테이블에 저장
        ProjectEntity project = ProjectEntity.builder()
                .projectId(UUID.randomUUID())
                .name(request.getName())
                .createdAt(LocalDateTime.now())
                .build();
        ProjectEntity savedProject = projectRepository.save(project);

        //유저 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        //개인별 프로젝트 테이블에 저장(이름은 null)
        UserProjectAliasEntity alias = UserProjectAliasEntity.builder()
                .aliasId(UUID.randomUUID())
                .project(savedProject)
                .user(user)
                .customName(null)
                .build();
        aliasRepository.save(alias);

        // 응답 반환
        return CreateProjectResponse.builder()
                .projectId(savedProject.getProjectId())
                .name(savedProject.getName())
                .createdAt(savedProject.getCreatedAt())
                .build();
    }
}