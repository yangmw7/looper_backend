package com.example.game_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // CSRF 보안 비활성화 (개발 중에는 OK)
                .authorizeHttpRequests()
                .requestMatchers("/join", "/hello", "/h2-console/**").permitAll() // 여기는 인증 없이 접근 가능
                .anyRequest().authenticated() // 나머지는 인증 필요
                .and()
                .headers().frameOptions().disable(); // H2 콘솔 사용을 위한 설정

        return http.build();
    }
}
