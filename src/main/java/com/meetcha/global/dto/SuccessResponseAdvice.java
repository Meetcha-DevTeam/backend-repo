package com.meetcha.global.dto;

import com.meetcha.global.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.meetcha")
public class SuccessResponseAdvice implements ResponseBodyAdvice<Object> {

    //swagger랑 actuator은 감싸지 않음
    private static final String[] ESCAPE_PATTERNS = {
            "/v3/api-docs", "/swagger-ui", "/swagger-ui.html",
            "/swagger-resources", "/webjars", "/actuator"
    };

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        String requestUri = request.getURI().getPath();

        for (String pattern : ESCAPE_PATTERNS) {
            if (requestUri.startsWith(pattern)) {
                return body;
            }
        }

        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int statusCode = servletResponse.getStatus();
        String method = ((ServletServerHttpRequest) request).getServletRequest().getMethod();

        if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
            return ApiResponse.success(requestUri, statusProvider(method), "요청에 성공하였습니다.", body);
        }

        return body;
    }

    private int statusProvider(String method) {
        return switch (method) {
            case "POST" -> 201;
            case "DELETE" -> 204;
            default -> 200;
        };
    }
}
