package com.meetcha.project.service;

import com.meetcha.global.exception.ConflictException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.project.domain.ProjectEntity;
import com.meetcha.project.domain.ProjectRepository;
import com.meetcha.project.domain.UserProjectAliasRepository;
import com.meetcha.project.dto.CreateProjectRequest;
import com.meetcha.project.dto.CreateProjectResponse;
import com.meetcha.project.dto.GetProjectsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.config.ConfigDataException;
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

    //프로젝트 생성
    private final ProjectRepository projectRepository;

    public CreateProjectResponse createProject(CreateProjectRequest request){
        //중복된 이름 확인
        if (projectRepository.findByName(request.getName()).isPresent()) {
            throw new ConflictException(ErrorCode.DUPLICATE_PROJECT_NAME);
        }

        //저장
        ProjectEntity project = ProjectEntity.builder()
                .name(request.getName())
                .createdAt(LocalDateTime.now())
                .build();
        ProjectEntity save = projectRepository.save(project);

        return CreateProjectResponse.builder()
                .projectId(save.getProjectId())
                .name(save.getName())
                .createdAt(save.getCreatedAt())
                .build();
    }
}