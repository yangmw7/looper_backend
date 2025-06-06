package com.example.game_backend.service;

import com.example.game_backend.controller.dto.PostRequest;
import com.example.game_backend.repository.entity.Image;
import com.example.game_backend.repository.entity.Post;
import com.example.game_backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    /**
     * application.properties 에서 가져오는 upload-dir 값 (예: "upload-dir")
     * 실제 저장 위치는 <프로젝트 루트>/upload-dir 로 강제 지정합니다.
     */
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    @Transactional
    public Long save(PostRequest request, String writer) {
        // 1) Post 엔티티 기본 정보 세팅
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .writer(writer)
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // build() 사용 시 images가 null일 수 있으므로 빈 리스트로 초기화
        if (post.getImages() == null) {
            post.setImages(new ArrayList<>());
        }

        // 2) MultipartFile 배열이 있으면 반복해서 저장하고 Image 엔티티로 만들어서 post에 추가
        MultipartFile[] files = request.getImageFiles();
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String url = storeFileAndGetUrl(file);
                    Image imgEntity = Image.builder()
                            .filePath(url)   // "/images/{savedFileName}" 형태 URL
                            .post(post)
                            .build();
                    post.getImages().add(imgEntity);
                }
            }
        }

        // 3) post + images 전부 저장 (cascade = ALL 덕분에 Image들도 함께 저장됨)
        Post saved = postRepository.save(post);
        return saved.getId();
    }

    @Override
    public String storeFileAndGetUrl(MultipartFile file) {
        try {
            // 1) 프로젝트 루트 디렉토리 기준 절대 경로 계산
            Path projectRoot = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
            Path uploadPath = projectRoot.resolve(uploadDir);

            // 2) 업로드 디렉토리가 없으면 생성
            if (Files.notExists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 3) UUID + 확장자 포맷으로 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String savedFileName = UUID.randomUUID().toString() + ext;

            // 4) 실제 저장될 대상 경로
            Path targetPath = uploadPath.resolve(savedFileName);

            // 5) 파일 복사 (기존 파일이 있으면 덮어쓰기)
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 6) 클라이언트가 접근 가능한 URL 리턴 (WebConfig에서 /images/** 경로로 매핑 예정)
            return "/images/" + savedFileName;

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}
