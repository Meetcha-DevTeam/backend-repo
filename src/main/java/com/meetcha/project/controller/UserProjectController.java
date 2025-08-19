package com.meetcha.project.controller;

import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.util.AuthHeaderUtils;
import com.meetcha.project.dto.CreateProjectRequest;
import com.meetcha.project.dto.CreateProjectResponse;
import com.meetcha.project.dto.GetProjectsDto;
import com.meetcha.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserProjectController {

    private final JwtProvider jwtProvider;
    private final ProjectService projectService;

    //프로젝트 조회
    @GetMapping("/projects")
    public ApiResponse<List<GetProjectsDto>> getUserProjects(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));
        List<GetProjectsDto> projects = projectService.getUserProjects(userId);

        return ApiResponse.success(200, "프로젝트 목록 조회 성공", projects);
    }

    //프로젝트 생성
    @PostMapping("/projects")
    public ApiResponse<CreateProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));
        CreateProjectResponse response = projectService.createProject(request, userId);

        return ApiResponse.success(201, "프로젝트 생성 성공", response);}

}