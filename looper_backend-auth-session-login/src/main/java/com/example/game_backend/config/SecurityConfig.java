package com.example.game_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil; // ✅ JwtUtil 주입

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // CSRF 비활성화
                .authorizeHttpRequests()

                /*.requestMatchers(
                        "/join", "/api/login", "/hello", "/h2-console/**",
                        "/join-form", "/login-form", "/home", "/logout","/find-id", "/find-id-result"
                ).permitAll() 테스트용 주석처리*/
                .anyRequest().permitAll() // ✅ 모든 요청 허용
                .and()
                .logout(logout -> logout.disable()) // 🔥 Spring Security 기본 로그아웃 기능 꺼줌
                .headers().frameOptions().disable(); // H2 콘솔용


        // ✅ JWT 인증 필터 등록
        http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

