package com.example.game_backend.config;

import com.example.game_backend.interceptor.PenaltyCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final PenaltyCheckInterceptor penaltyCheckInterceptor;

    /**
     * 1) CORS 설정
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "https://looper-game.duckdns.org")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    /**
     * 2) Interceptor 등록 (제재 체크)
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(penaltyCheckInterceptor)
                .addPathPatterns("/api/**")  // 모든 API에 적용
                .excludePathPatterns(
                        "/api/auth/**",           // 인증 관련 제외
                        "/api/chatbot/**",        // 챗봇 API 제외 (모든 사용자 접근 가능)
                        "/api/mypage/**",         // 마이페이지는 제외 (제재 확인용)
                        "/api/notifications/**"   // 알림도 제외 (제재 알림 받아야 함)
                );
    }
}