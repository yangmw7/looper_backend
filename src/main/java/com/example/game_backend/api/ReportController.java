package com.example.game_backend.api;

import com.example.game_backend.controller.dto.report.ReportCreateRequest;
import com.example.game_backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 게시글 신고
     */
    @PostMapping("/posts/{postId}")
    public ResponseEntity<Map<String, Long>> reportPost(
            @PathVariable Long postId,
            @RequestBody ReportCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long reportId = reportService.createPostReport(
                postId,
                userDetails.getUsername(),
                request
        );

        return ResponseEntity.ok(Map.of("reportId", reportId));
    }

    /**
     * 댓글 신고 (커뮤니티)
     */
    @PostMapping("/comments/{commentId}")
    public ResponseEntity<Map<String, Long>> reportComment(
            @PathVariable Long commentId,
            @RequestBody ReportCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long reportId = reportService.createCommentReport(
                commentId,
                userDetails.getUsername(),
                request
        );

        return ResponseEntity.ok(Map.of("reportId", reportId));
    }

    // ========== 공지사항 댓글 신고 추가 ==========

    /**
     * 공지사항 댓글 신고
     */
    @PostMapping("/announcement-comments/{commentId}")
    public ResponseEntity<Map<String, Long>> reportAnnouncementComment(
            @PathVariable Long commentId,
            @RequestBody ReportCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long reportId = reportService.createAnnouncementCommentReport(
                commentId,
                userDetails.getUsername(),
                request
        );

        return ResponseEntity.ok(Map.of("reportId", reportId));
    }
}