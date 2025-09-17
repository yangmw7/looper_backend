// src/main/java/com/example/game_backend/service/CommentService.java
package com.example.game_backend.service;

import com.example.game_backend.controller.dto.CommentRequest;
import com.example.game_backend.controller.dto.CommentResponse;
import com.example.game_backend.repository.entity.Comment;

import java.util.List;

public interface CommentService {
    Comment saveComment(Long postId, CommentRequest commentRequest);
    List<CommentResponse> getCommentsByPostId(Long postId);

    Comment updateComment(Long commentId, CommentRequest commentRequest);
    void deleteComment(Long commentId);
}
