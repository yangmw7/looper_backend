package com.example.game_backend.api;

import com.example.game_backend.controller.dto.CommentRequest;
import com.example.game_backend.controller.dto.CommentResponse;
import com.example.game_backend.repository.entity.Comment;
import com.example.game_backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class CommentController {

    private final CommentService commentService;

    // ✅ Logger 추가
    private static final Logger log = LoggerFactory.getLogger(CommentController.class);

    // ✅ 댓글 작성
    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> writeComment(
            @PathVariable Long postId,
            @RequestBody CommentRequest commentRequest) {

        // 🔥 로그 찍기
        log.info("📨 댓글 작성 요청: postId={}, content={}", postId, commentRequest.getContent());
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("🔐 JWT에서 추출된 username = {}", username);

        Comment savedComment = commentService.saveComment(postId, commentRequest);

        // ✅ 저장 결과 로그
        log.info("✅ 저장된 댓글 ID: {}", savedComment.getId());

        return ResponseEntity.ok(savedComment);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        log.info("🔍 댓글 목록 조회 요청: postId={}", postId);

        List<CommentResponse> dtos = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(dtos);
    }
}
