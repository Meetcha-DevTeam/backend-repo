package com.meetcha.project.service;

import com.meetcha.project.domain.UserProjectAliasRepository;
import com.meetcha.project.dto.ProjectSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProjectService {
    //특정 사용자가 참여한 프로젝트 목록 조회

    private final UserProjectAliasRepository aliasRepository;

    public List<ProjectSummaryDto> getUserProjects(UUID userId) {
        return aliasRepository.findProjectsByUserId(userId);
    }
}