package com.example.game_backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // 헤더가 없거나 "Bearer "로 시작하지 않으면 파싱 시도 없이 바로 통과
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer " 이후 토큰 부분만 추출
        String token = authHeader.substring(7);
        try {
            // 토큰을 파싱하여 username을 꺼냄 (유효성 검사 포함)
            String username = jwtUtil.extractUsername(token);

            // 인증 객체 생성 후 SecurityContext에 저장
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception e) {
            // 토큰 검증/파싱 실패 시 로그만 남기고 인증 없이 계속 진행
            System.out.println("❌ JWT 인증 실패: " + e.getMessage());
        }

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // 로그인, 회원가입 엔드포인트는 JWT 인증을 건너뜀
        if (path.equals("/api/login") || path.equals("/api/join")) {
            return true;
        }
        // CORS preflight 요청(OPTIONS)도 건너뜀
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return false;
    }
}
