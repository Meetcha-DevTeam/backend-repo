package com.meetcha.auth.config;

import com.meetcha.auth.jwt.JwtAuthenticationFilter;
import com.meetcha.auth.jwt.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    public SecurityConfig(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .authorizeHttpRequests(auth -> auth
                                .anyRequest().permitAll()  // 개발 중엔 전부 오픈
                        // 배포 시에는 아래처럼 수정:
                        // .requestMatchers("/oauth/google").permitAll()
                        // .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
