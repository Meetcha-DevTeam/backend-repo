package com.meetcha.project.controller;

import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.annotation.AuthUser;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.project.dto.CreateProjectRequest;
import com.meetcha.project.dto.CreateProjectResponse;
import com.meetcha.project.dto.GetProjectsDto;
import com.meetcha.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserProjectController {

    private final ProjectService projectService;

    //프로젝트 조회
    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<List<GetProjectsDto>>> getUserProjects(
            @AuthUser UUID userId
    ) {
        List<GetProjectsDto> projects = projectService.getUserProjects(userId);

        return ResponseEntity
                .ok(ApiResponse.success(200, "프로젝트 목록 조회 성공", projects));
    }

    //프로젝트 생성
    @PostMapping("/projects")
    public ResponseEntity<ApiResponse<CreateProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthUser UUID userId
    ) {
        CreateProjectResponse response = projectService.createProject(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "프로젝트 생성 성공", response));
    }
}