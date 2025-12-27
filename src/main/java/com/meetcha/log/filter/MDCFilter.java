package com.meetcha.log.filter;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jboss.logging.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MDCFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            String requestId = getRequestId(request);
            MDC.put("requestId", requestId);
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private static String getRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        if(requestId == null || requestId.isEmpty()){
            requestId = UUID.randomUUID().toString().replaceAll("-", "");
        }
        return requestId;
    }
}
