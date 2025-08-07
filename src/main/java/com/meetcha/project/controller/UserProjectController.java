package com.meetcha.project.controller;

import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.exception.UnauthorizedException;
import com.meetcha.project.dto.GetProjectsDto;
import com.meetcha.project.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

//alias랑 관련된 내용은 /user로 매핑
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserProjectController {

    private final ProjectService userProjectService;
    private final JwtProvider jwtProvider;
    @GetMapping("/projects")
    public ApiResponse<List<GetProjectsDto>> getUserProjects(HttpServletRequest request)
    {
        String token = extractToken(request);
        UUID userId = jwtProvider.getUserId(token); // 직접 userId 추출

        List<GetProjectsDto> projects = userProjectService.getUserProjects(userId);
        return ApiResponse.success(200, "프로젝트 목록 조회 성공", projects);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        throw new UnauthorizedException(ErrorCode.MISSING_AUTH_TOKEN);
    }
}