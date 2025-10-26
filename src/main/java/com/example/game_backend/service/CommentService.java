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
    void toggleLike(Long commentId, String username);
    Map<String, Object> toggleLikeAndGetStatus(Long commentId, String username);
    List<CommentResponse> getCommentsByUsername(String username);
}