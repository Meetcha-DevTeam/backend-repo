package Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                                .anyRequest().permitAll()  //개발할땐 우선 다 오픈!! 대신 배포 전에 밑 코드로 수정필요
                        //.requestMatchers("/oauth/google").permitAll() //로그인은 누구나
                        //.anyRequest().authenticated()                //나머지는 인증 필요
                )
                .build();
    }
}
