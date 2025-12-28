package com.meetcha.auth.config;

import com.meetcha.auth.jwt.UserDetailsResolver;
import com.meetcha.log.intercepter.LoggingIntercepter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@AllArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final UserDetailsResolver userDetailsResolver;
    private final LoggingIntercepter loggingIntercepter;

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userDetailsResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingIntercepter).addPathPatterns("/**");
    }
}
