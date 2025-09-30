package com.example.game_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

    private final JwtUtil jwtUtil;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1) CORS, CSRF 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                // 2) URL별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증/회원가입/토큰 재발급은 모두 허용
                        .requestMatchers("/api/auth/**").permitAll()

                        // 관리자 페이지 API
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 아이템/NPC 관리 API 전체 관리자만 허용 (GET 포함)
                        .requestMatchers("/api/items/**").hasRole("ADMIN")
                        .requestMatchers("/api/npcs/**").hasRole("ADMIN")

                        // OPTIONS 요청은 CORS preflight용으로 항상 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 그 외 나머지는 기본 허용
                        .anyRequest().permitAll()
                )

                // 3) 기본 로그아웃 비활성화
                .logout(logout -> logout.disable())

                // 4) H2 콘솔용 frameOptions 비활성화
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        // 5) JWT 인증 필터 등록
        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtUtil),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "https://looper-game.duckdns.org"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
