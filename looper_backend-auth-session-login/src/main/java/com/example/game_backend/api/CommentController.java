package com.example.game_backend.api;

import com.example.game_backend.controller.dto.CommentRequest;
import com.example.game_backend.controller.dto.CommentResponse;
import com.example.game_backend.repository.entity.Comment;
import com.example.game_backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class CommentController {

    private final CommentService commentService;
    private static final Logger log = LoggerFactory.getLogger(CommentController.class);

    // ── 댓글 작성 ──
    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> writeComment(
            @PathVariable Long postId,
            @RequestBody CommentRequest commentRequest) {

        log.info("📨 댓글 작성 요청: postId={}, content={}", postId, commentRequest.getContent());
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("🔐 JWT에서 추출된 username = {}", username);

        Comment savedComment = commentService.saveComment(postId, commentRequest);
        log.info("✅ 저장된 댓글 ID: {}", savedComment.getId());

        // 저장된 Comment 엔티티를 CommentResponse DTO로 변환해서 반환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        CommentResponse responseDto = CommentResponse.builder()
                .id(savedComment.getId())
                .content(savedComment.getContent())
                .writerNickname(savedComment.getNickname())
                .createdAt(savedComment.getCreatedAt().format(formatter))
                .build();

        return ResponseEntity.ok(responseDto);
    }

    // ── 댓글 목록 조회 ──
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        log.info("🔍 댓글 목록 조회 요청: postId={}", postId);

        List<CommentResponse> dtos = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(dtos);
    }

    // ── 댓글 수정 ──
    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> editComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentRequest commentRequest) {

        log.info("✏️ 댓글 수정 요청: postId={}, commentId={}, newContent={}",
                postId, commentId, commentRequest.getContent());

        // 1) 서비스 계층에서 작성자 검사 후 실제 댓글 수정
        Comment updated = commentService.updateComment(commentId, commentRequest);
        log.info("✅ 수정된 댓글 ID: {}", updated.getId());

        // 2) 수정된 Comment 엔티티를 CommentResponse DTO로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        CommentResponse responseDto = CommentResponse.builder()
                .id(updated.getId())
                .content(updated.getContent())
                .writerNickname(updated.getNickname())
                .createdAt(updated.getCreatedAt().format(formatter))
                .build();

        return ResponseEntity.ok(responseDto);
    }

    // ── 댓글 삭제 ──
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> removeComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {

        log.info("🗑️ 댓글 삭제 요청: postId={}, commentId={}", postId, commentId);
        try {
            commentService.deleteComment(commentId);
            log.info("✅ 삭제된 댓글 ID: {}", commentId);
            return ResponseEntity.noContent().build();

        } catch (AccessDeniedException ex) {
            log.warn("⚠️ 삭제 권한 없음: commentId={}, user={}",
                    commentId,
                    SecurityContextHolder.getContext().getAuthentication().getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (IllegalArgumentException ex) {
            log.error("❌ 삭제 실패: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
