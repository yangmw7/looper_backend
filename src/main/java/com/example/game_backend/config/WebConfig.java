package com.example.game_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 1) CORS 설정
     *    - 프론트엔드 주소(React dev 서버)와 통신할 때 발생하는 CORS 이슈 방지
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")       // /api/** 경로로 들어오는 요청만 허용
                .allowedOrigins("http://localhost:5173") // React dev 서버 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")              // 모든 헤더 허용 (Authorization 포함)
                .allowCredentials(true);          // 쿠키(Cookie) 인증도 허용 (필요 시)
    }

    /**
     * 2) 정적 리소스 매핑 (파일 서빙)
     *    - 브라우저에서 /images/** 로 요청하면 실제 upload-dir 폴더를 뒤져서 파일을 내보냅니다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // “프로젝트 루트/user.dir/upload-dir/” 위치를 매핑
        String projectRoot = System.getProperty("user.dir");
        // upload-dir 폴더 경로 (PostServiceImpl에서 여기에 저장됨)
        String uploadPath = "file:" + projectRoot + "/upload-dir/";

        registry.addResourceHandler("/images/**")
                .addResourceLocations(uploadPath);
        // 이제 브라우저에서 http://localhost:8080/images/uuid_abc.png → 실제 /upload-dir/uuid_abc.png 파일을 읽어옴
    }
}
