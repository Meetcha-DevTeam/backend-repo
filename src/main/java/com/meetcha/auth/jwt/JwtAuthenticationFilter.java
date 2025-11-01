package com.meetcha.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetcha.global.dto.ApiResponse;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCodeBase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        try {
            if (token != null && jwtProvider.validateToken(token)) {
                UUID userId = jwtProvider.getUserId(token);
                String email = jwtProvider.getEmail(token);

                JwtUserPrincipal principal = new JwtUserPrincipal(userId, email);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (CustomException e) {
            // JWT 관련 CustomException 발생 시 ApiResponse 형식으로 응답
            writeErrorResponse(response, e.getErrorCode(), request.getRequestURI());
            return;
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private void writeErrorResponse(HttpServletResponse response, ErrorCodeBase errorCode, String path) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json; charset=UTF-8");

        ApiResponse<Void> errorResponse = ApiResponse.error(path, errorCode);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()); // ✅ LocalDateTime 지원
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

}
