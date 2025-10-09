package com.example.game_backend.service;

import com.example.game_backend.controller.dto.PostRequest;
import com.example.game_backend.repository.entity.Member;
import org.springframework.web.multipart.MultipartFile;

public interface PostService {
    Long save(PostRequest request, Member writer);
    void updatePost(Long postId, PostRequest request);
    void deletePost(Long postId);
    String storeFileAndGetUrl(MultipartFile file);
}