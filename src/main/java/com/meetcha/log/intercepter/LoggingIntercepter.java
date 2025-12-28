package com.meetcha.log.intercepter;

import com.meetcha.log.util.LoggedJsonMasker;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class LoggingIntercepter implements HandlerInterceptor {
    private static final int MAX_LOG_BYTES = 8 * 1024;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 요청 계속 진행
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        logBasicRequestInfo(request);
        ContentCachingRequestWrapper requestWrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if(requestWrapper != null && hasLoggableBody(request)){
            logRequestBody(requestWrapper, request);
        }

        logBasicResponseInfo(response);
        ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if(responseWrapper != null && hasLoggableBody(response)){
            logResponseBody(responseWrapper, response);
        }
    }

    private void logBasicRequestInfo(HttpServletRequest request) {
        if (request.getParameterNames().hasMoreElements()){
            log.info("API Request : clientIP = [{}] method = [{}] URL = [{}] params = [{}]", getClientIP(request), request.getMethod(), request.getRequestURL(), getRequestParams(request));
            return;
        }
        log.info("API Request : clientIP = [{}] method = [{}] URL = [{}]", getClientIP(request), request.getMethod(), request.getRequestURL());
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


    private static boolean hasLoggableBody(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.startsWith(MediaType.APPLICATION_JSON_VALUE);
    }

    private void logRequestBody(ContentCachingRequestWrapper requestWrapper, HttpServletRequest request) {
        byte[] bytes = requestWrapper.getContentAsByteArray();
        if (bytes.length == 0){
            return;
        }
        Charset charset = (request.getCharacterEncoding() != null)
                ? Charset.forName(request.getCharacterEncoding()) : StandardCharsets.UTF_8;
        String body = new String(bytes, charset);
        String masked = LoggedJsonMasker.mask(body);
        log.info("API Request : body = [{}]", abbreviate(masked));
    }

    private String abbreviate(String s) {
        if (s == null) return null;
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        if (b.length <= MAX_LOG_BYTES) return s;
        return new String(b, 0, MAX_LOG_BYTES, StandardCharsets.UTF_8) + "...(truncated)";
    }

    private static void logBasicResponseInfo(HttpServletResponse response) {
        log.info("API Response : Status = [{}]", response.getStatus());
    }

    private boolean hasLoggableBody(HttpServletResponse response) {
        String contentType = response.getContentType();
        if(contentType != null && contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)){
            return true;
        }
        return false;
    }

    private void logResponseBody(ContentCachingResponseWrapper responseWrapper, HttpServletResponse response) throws Exception {
        byte[] bytes = responseWrapper.getContentAsByteArray();
        if (bytes.length == 0){
            return;
        }
        Charset charset = (response.getCharacterEncoding() != null)
                ? Charset.forName(response.getCharacterEncoding()) : StandardCharsets.UTF_8;
        String body = new String(bytes, charset);
        String masked = LoggedJsonMasker.mask(body);
        log.info("API Response : body = [{}]", abbreviate(masked));
    }
}
