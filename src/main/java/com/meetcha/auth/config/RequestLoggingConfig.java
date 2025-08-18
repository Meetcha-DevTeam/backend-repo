package com.meetcha.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingConfig {
    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);     // 쿼리 파라미터 로그 출력
        filter.setIncludePayload(true);         // 요청 바디 로그 출력
        filter.setMaxPayloadLength(10000);      // 최대 바디 길이 제한 (10KB)
        filter.setIncludeHeaders(true);         // 헤더까지 로그 출력
        filter.setAfterMessagePrefix("📥 REQUEST DATA : "); // 로그 앞에 붙일 prefix
        return filter;
    }
}