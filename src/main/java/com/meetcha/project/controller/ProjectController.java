package com.meetcha.project.controller;

import com.meetcha.global.dto.ApiResponse;
import com.meetcha.project.dto.CreateProjectRequest;
import com.meetcha.project.dto.CreateProjectResponse;
import com.meetcha.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    //프로젝트 생성(이름 == 프로젝트이름)
    @PostMapping
    public ResponseEntity<ApiResponse<CreateProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request
    ) {
        CreateProjectResponse response = projectService.createProject(request);

        return ResponseEntity
                .status(201)
                .body(ApiResponse.success(201, "프로젝트가 성공적으로 생성되었습니다.", response));
    }
}
