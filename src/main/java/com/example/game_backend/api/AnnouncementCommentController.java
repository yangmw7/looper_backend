package com.example.game_backend.api;

import com.example.game_backend.config.JwtUtil;
import com.example.game_backend.controller.dto.announcement.AnnouncementCommentRequest;
import com.example.game_backend.controller.dto.announcement.AnnouncementCommentResponse;
import com.example.game_backend.service.AnnouncementCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementCommentController {

    private final AnnouncementCommentService commentService;
    private final JwtUtil jwtUtil;

    @PostMapping("/{announcementId}/comments")
    public ResponseEntity<String> createComment(
            @PathVariable Long announcementId,
            @RequestBody AnnouncementCommentRequest request,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("공지사항에는 댓글 기능이 제공되지 않습니다.");
    }

    @GetMapping("/{announcementId}/comments")
    public ResponseEntity<List<AnnouncementCommentResponse>> getComments(
            @PathVariable Long announcementId) {

        return ResponseEntity.ok(Collections.emptyList());
    }

    @PutMapping("/{announcementId}/comments/{commentId}")
    public ResponseEntity<String> updateComment(
            @PathVariable Long announcementId,
            @PathVariable Long commentId,
            @RequestBody AnnouncementCommentRequest request,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("공지사항에는 댓글 기능이 제공되지 않습니다.");
    }

    @DeleteMapping("/{announcementId}/comments/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long announcementId,
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("공지사항에는 댓글 기능이 제공되지 않습니다.");
    }

    @PostMapping("/{announcementId}/comments/{commentId}/like")
    public ResponseEntity<String> toggleCommentLike(
            @PathVariable Long announcementId,
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("공지사항에는 댓글 기능이 제공되지 않습니다.");
    }
}