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
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // 추가: 스프링 빈으로 주입

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // 인증/회원가입/토큰 재발급은 모두 허용
                        .requestMatchers("/api/auth/**").permitAll()

                        // 마이페이지: 로그인 유저만 가능 (추가)
                        .requestMatchers("/api/mypage/**").authenticated()

                        // 신고하기: 로그인 유저만 가능
                        .requestMatchers("/api/reports/**").authenticated()

                        // 관리자 페이지 API
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // GET 요청은 모두 허용 (게임 가이드/클라이언트용)
                        .requestMatchers(HttpMethod.GET, "/api/items/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/npcs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/skills/**").permitAll()

                        // POST/PUT/DELETE는 관리자만
                        .requestMatchers("/api/items/**").hasRole("ADMIN")
                        .requestMatchers("/api/npcs/**").hasRole("ADMIN")
                        .requestMatchers("/api/skills/**").hasRole("ADMIN")

                        // OPTIONS 요청은 CORS preflight용으로 항상 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 그 외 나머지는 기본 허용
                        .anyRequest().permitAll()
                )

                .logout(logout -> logout.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        // 수정: 스프링 빈으로 주입받은 필터 사용
        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "https://looper-game.duckdns.org"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}