package com.example.game_backend.service;

import com.example.game_backend.controller.dto.PostRequest;
import com.example.game_backend.repository.PostRepository;
import com.example.game_backend.repository.entity.Image;
import com.example.game_backend.repository.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    @Transactional
    public Long save(PostRequest request, String writer) {
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .writer(writer)
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (post.getImages() == null) {
            post.setImages(new ArrayList<>());
        }

        MultipartFile[] files = request.getImageFiles();
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = storeFileAndGetUrl(file);
                    post.getImages().add(
                            Image.builder()
                                    .filePath(url)
                                    .post(post)
                                    .build()
                    );
                }
            }
        }

        return postRepository.save(post).getId();
    }

    @Override
    @Transactional
    public void updatePost(Long postId, PostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        // 제목/내용 업데이트
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setUpdatedAt(LocalDateTime.now());

        // 삭제 로직: keepUrls null 처리
        List<String> keepUrls = request.getKeepImageUrls() != null
                ? request.getKeepImageUrls()
                : new ArrayList<>();
        post.getImages().removeIf(img -> !keepUrls.contains(img.getFilePath()));

        // 새 이미지 추가 로직
        MultipartFile[] newFiles = request.getImageFiles();
        if (newFiles != null) {
            for (MultipartFile f : newFiles) {
                if (!f.isEmpty()) {
                    String url = storeFileAndGetUrl(f);
                    post.getImages().add(
                            Image.builder()
                                    .filePath(url)
                                    .post(post)
                                    .build()
                    );
                }
            }
        }
        // 변경 감지로 자동 저장
    }

    @Override
    public String storeFileAndGetUrl(MultipartFile file) {
        try {
            Path root = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
            Path uploadPath = root.resolve(uploadDir);

            if (Files.notExists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String orig = file.getOriginalFilename();
            String ext = (orig != null && orig.contains("."))
                    ? orig.substring(orig.lastIndexOf("."))
                    : "";
            String savedName = UUID.randomUUID().toString() + ext;
            Path target = uploadPath.resolve(savedName);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/images/" + savedName;

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}
