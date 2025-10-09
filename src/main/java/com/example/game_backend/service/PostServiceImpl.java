package com.example.game_backend.service;

import com.example.game_backend.controller.dto.PostRequest;
import com.example.game_backend.repository.PostRepository;
import com.example.game_backend.repository.entity.Image;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public Long save(PostRequest request, Member writer) {
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

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setUpdatedAt(LocalDateTime.now());

        List<String> keepUrls = request.getKeepImageUrls() != null
                ? request.getKeepImageUrls()
                : new ArrayList<>();

        post.getImages().removeIf(img -> {
            if (!keepUrls.contains(img.getFilePath())) {
                cloudinaryService.deleteImage(img.getFilePath());
                return true;
            }
            return false;
        });

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
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        // 게시글에 연결된 모든 이미지를 Cloudinary에서 삭제
        if (post.getImages() != null) {
            for (Image image : post.getImages()) {
                cloudinaryService.deleteImage(image.getFilePath());
            }
        }

        // DB에서 게시글 삭제 (cascade로 Image도 함께 삭제됨)
        postRepository.deleteById(postId);
    }

    @Override
    public String storeFileAndGetUrl(MultipartFile file) {
        String uniqueId = "post_" + System.currentTimeMillis();
        return cloudinaryService.uploadImage(file, uniqueId);
    }
}