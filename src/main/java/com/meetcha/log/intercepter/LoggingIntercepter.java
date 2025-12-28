package com.meetcha.log.intercepter;

import com.meetcha.log.servlet.CustomHttpRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class LoggingIntercepter implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getParameterNames().hasMoreElements()){
            log.info("API Request : clientIP = [{}] method = [{}] URL = [{}] params = [{}]", getClientIP(request), request.getMethod(), request.getRequestURL(), getRequestParams(request));
        }
        else{
            log.info("API Request : clientIP = [{}] method = [{}] URL = [{}]", getClientIP(request), request.getMethod(), request.getRequestURL());
        }

        // request가 CustomHttpRequestWrapper로 래핑되어 있는지 확인
        if(request instanceof CustomHttpRequestWrapper){
            CustomHttpRequestWrapper requestWrapper = (CustomHttpRequestWrapper) request;
            String requestBody = new String(requestWrapper.getRequestBody());

            // Request Body가 있을 경우 로깅
            if (!requestBody.isEmpty()) {
                log.info("API Request : body = [{}]", requestBody);
            }
        }

        // 요청 계속 진행
        return true;
    }

    private String getClientIP(HttpServletRequest request) {
        String clientIP = request.getHeader("X-Real-IP");
        if(clientIP == null || clientIP.isEmpty()){
            clientIP = request.getRemoteAddr();
        }
        return clientIP;
    }

    private Map<String, String> getRequestParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<String, String>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while(parameterNames.hasMoreElements()){
            String name = parameterNames.nextElement();
            params.put(name, request.getParameter(name));
        }
        return params;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.info("API Response : Status = [{}]", response.getStatus());
    }
}
