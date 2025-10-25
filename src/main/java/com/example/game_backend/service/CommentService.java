package com.example.game_backend.service;

import com.example.game_backend.controller.dto.CommentRequest;
import com.example.game_backend.controller.dto.CommentResponse;
import com.example.game_backend.repository.entity.Comment;

import java.util.List;
import java.util.Map;

public interface CommentService {
    Comment saveComment(Long postId, CommentRequest commentRequest);
    List<CommentResponse> getCommentsByPostId(Long postId);
    Comment updateComment(Long commentId, CommentRequest commentRequest);
    void deleteComment(Long commentId);

    // 댓글 좋아요
    void toggleLike(Long commentId, String username);

    // ⭐ 신규 추가: 댓글 좋아요 토글 후 상태 반환
    Map<String, Object> toggleLikeAndGetStatus(Long commentId, String username);
}