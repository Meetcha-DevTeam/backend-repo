package com.meetcha.log.filter;

import com.meetcha.log.servlet.CustomHttpRequestWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestWrapperFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String contentType = request.getContentType();
        if (contentType != null && contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
            // CustomHttpRequestWrapper로 감싼 후 체인 진행
            CustomHttpRequestWrapper requestWrapper = new CustomHttpRequestWrapper(request);
            chain.doFilter(requestWrapper, response);
            return;
        }
        chain.doFilter(request, response);
    }
}