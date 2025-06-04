package com.example.game_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                // 1) CORS 허용 설정 활성화
                .cors().and()

                // 2) CSRF 비활성화 (API 전용 서버라면 disable)
                .csrf().disable()

                // 3) 모든 요청을 인증 없이 허용
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )

                // 4) 기본 로그아웃 기능 비활성화
                .logout(logout -> logout.disable())

                // 5) H2 콘솔을 위한 frameOptions 비활성화 (만약 H2 콘솔을 사용 중이라면)
                .headers().frameOptions().disable();

        // 6) JWT 인증 필터 등록 (필요시 사용)
        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtUtil),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    // ---------------------------------------------
    // React(5173)에서 오는 요청을 CORS 차단 없이 받기 위한 설정
    // ---------------------------------------------
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // React 개발 서버가 돌아가는 주소를 허용
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        // HTTP 메서드는 필요에 따라 추가 (GET, POST, PUT, DELETE, OPTIONS 등)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 요청 시 들어오는 모든 헤더 허용 (Content-Type, Authorization 등)
        config.setAllowedHeaders(List.of("*"));
        // 자격 증명(쿠키, 인증 토큰) 사용 시 true (필요하지 않다면 false로 설정)
        config.setAllowCredentials(true);

        // 최종적으로 모든 경로("/**")에 위 CORS 설정을 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
