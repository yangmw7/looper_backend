package com.example.game_backend.service;

import com.example.game_backend.controller.dto.PostRequest;
import com.example.game_backend.repository.entity.Member;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface PostService {
    Long save(PostRequest request, Member writer);
    void updatePost(Long postId, PostRequest request);
    void deletePost(Long postId);
    String storeFileAndGetUrl(MultipartFile file);

    // 게시글 좋아요
    void toggleLike(Long postId, String username);

    // ⭐ 신규 추가: 게시글 좋아요 토글 후 상태 반환
    Map<String, Object> toggleLikeAndGetStatus(Long postId, String username);

    // ⭐ 신규 추가: 게시글 좋아요 상태 조회
    boolean isPostLikedByUser(Long postId, String username);

    // ⭐ 신규 추가: 댓글 좋아요 상태 조회
    List<Long> getLikedCommentIds(Long postId, String username);
}