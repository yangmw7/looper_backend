package com.example.game_backend.api;

import com.example.game_backend.config.JwtUtil;
import com.example.game_backend.controller.dto.announcement.AnnouncementCommentRequest;
import com.example.game_backend.controller.dto.announcement.AnnouncementCommentResponse;
import com.example.game_backend.service.AnnouncementCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementCommentController {

    private final AnnouncementCommentService commentService;
    private final JwtUtil jwtUtil;

    /**
     * 댓글 작성 (일반 댓글 + 대댓글)
     */
    @PostMapping("/{announcementId}/comments")
    public ResponseEntity<Void> createComment(
            @PathVariable Long announcementId,
            @RequestBody AnnouncementCommentRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);

        commentService.saveComment(announcementId, request, username);
        return ResponseEntity.ok().build();
    }

    /**
     * 댓글 목록 조회 (대댓글 포함)
     */
    @GetMapping("/{announcementId}/comments")
    public ResponseEntity<List<AnnouncementCommentResponse>> getComments(
            @PathVariable Long announcementId) {

        List<AnnouncementCommentResponse> comments = commentService.getComments(announcementId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/{announcementId}/comments/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable Long announcementId,
            @PathVariable Long commentId,
            @RequestBody AnnouncementCommentRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);

        commentService.updateComment(commentId, request, username);
        return ResponseEntity.ok().build();
    }

    /**
     * 댓글 삭제 (본인 또는 관리자)
     */
    @DeleteMapping("/{announcementId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long announcementId,
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);

        commentService.deleteComment(commentId, username);
        return ResponseEntity.noContent().build();
    }

    /**
     * 댓글 좋아요 토글
     */
    @PostMapping("/{announcementId}/comments/{commentId}/like")
    public ResponseEntity<Void> toggleCommentLike(
            @PathVariable Long announcementId,
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);

        commentService.toggleLike(commentId, username);
        return ResponseEntity.ok().build();
    }
}