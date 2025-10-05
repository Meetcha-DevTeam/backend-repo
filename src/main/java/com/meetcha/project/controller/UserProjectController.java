package com.meetcha.project.controller;

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
    public List<GetProjectsDto> getUserProjects(
            @AuthUser UUID userId
    ) {
        return projectService.getUserProjects(userId);
    }

    //프로젝트 생성
    @PostMapping("/projects")
    public CreateProjectResponse createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthUser UUID userId
    ) {
        return projectService.createProject(request, userId);
    }
}