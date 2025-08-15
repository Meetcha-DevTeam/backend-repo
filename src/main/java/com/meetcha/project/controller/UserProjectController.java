package com.meetcha.project.controller;

import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.util.AuthHeaderUtils;
import com.meetcha.project.dto.CreateProjectRequest;
import com.meetcha.project.dto.CreateProjectResponse;
import com.meetcha.project.dto.GetProjectsDto;
import com.meetcha.project.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserProjectController {

    private final ProjectService userProjectService;
    private final JwtProvider jwtProvider;
    private final ProjectService projectService;

    //프로젝트 조회
    //alias랑 관련된 내용은 /user로 매핑
    @GetMapping("/user/projects")
    public ApiResponse<List<GetProjectsDto>> getUserProjects(HttpServletRequest request,
                                                             @RequestHeader("Authorization") String authorizationHeader
    ) {
        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));

        List<GetProjectsDto> projects = userProjectService.getUserProjects(userId);
        return ApiResponse.success(200, "프로젝트 목록 조회 성공", projects);
    }

    //프로젝트 생성(이름 == 프로젝트이름)
    @PostMapping("/projects/create")
    public ResponseEntity<ApiResponse<CreateProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request, HttpServletRequest httpServletRequest,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        UUID userId = jwtProvider.getUserId(AuthHeaderUtils.extractBearerToken(authorizationHeader));

        CreateProjectResponse response = projectService.createProject(request, userId);

        return ResponseEntity
                .status(201)
                .body(ApiResponse.success(201, "프로젝트가 성공적으로 생성되었습니다.", response));
    }

}