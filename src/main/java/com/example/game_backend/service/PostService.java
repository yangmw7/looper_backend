package com.example.game_backend.service;

import com.example.game_backend.controller.dto.PostRequest;
import com.example.game_backend.controller.dto.PostResponse;
import com.example.game_backend.repository.entity.Member;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface PostService {
    Long save(PostRequest request, Member writer);
    void updatePost(Long postId, PostRequest request);
    void deletePost(Long postId);
    String storeFileAndGetUrl(MultipartFile file);

    void toggleLike(Long postId, String username);
    Map<String, Object> toggleLikeAndGetStatus(Long postId, String username);
    boolean isPostLikedByUser(Long postId, String username);
    List<Long> getLikedCommentIds(Long postId, String username);

    List<PostResponse> getMyPosts(String username);
}