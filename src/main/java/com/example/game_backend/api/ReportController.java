package com.example.game_backend.api;

import com.example.game_backend.controller.dto.report.ReportCreateRequest;
import com.example.game_backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 게시글 신고
     */
    @PostMapping("/posts/{postId}")
    public ResponseEntity<?> reportPost(
            @PathVariable Long postId,
            @RequestBody ReportCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            log.info("📋 게시글 신고 요청 - postId: {}, user: {}", postId, userDetails.getUsername());

            Long reportId = reportService.createPostReport(
                    postId,
                    userDetails.getUsername(),
                    request
            );

            log.info("✅ 게시글 신고 성공 - reportId: {}", reportId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "reportId", reportId,
                    "message", "신고가 접수되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 게시글 신고 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", "BAD_REQUEST",
                            "message", e.getMessage()
                    ));

        } catch (IllegalStateException e) {
            log.warn("⚠️ 게시글 신고 실패 - 비즈니스 로직 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "success", false,
                            "error", "CONFLICT",
                            "message", e.getMessage()
                    ));

        } catch (Exception e) {
            log.error("❌ 게시글 신고 실패 - 서버 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "INTERNAL_SERVER_ERROR",
                            "message", "신고 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                    ));
        }
    }

    /**
     * 댓글 신고 (커뮤니티)
     */
    @PostMapping("/comments/{commentId}")
    public ResponseEntity<?> reportComment(
            @PathVariable Long commentId,
            @RequestBody ReportCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            log.info("💬 댓글 신고 요청 - commentId: {}, user: {}", commentId, userDetails.getUsername());

            Long reportId = reportService.createCommentReport(
                    commentId,
                    userDetails.getUsername(),
                    request
            );

            log.info("✅ 댓글 신고 성공 - reportId: {}", reportId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "reportId", reportId,
                    "message", "신고가 접수되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 댓글 신고 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", "BAD_REQUEST",
                            "message", e.getMessage()
                    ));

        } catch (IllegalStateException e) {
            log.warn("⚠️ 댓글 신고 실패 - 비즈니스 로직 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "success", false,
                            "error", "CONFLICT",
                            "message", e.getMessage()
                    ));

        } catch (Exception e) {
            log.error("❌ 댓글 신고 실패 - 서버 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "INTERNAL_SERVER_ERROR",
                            "message", "신고 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                    ));
        }
    }

    /**
     * 공지사항 댓글 신고
     */
    @PostMapping("/announcement-comments/{commentId}")
    public ResponseEntity<?> reportAnnouncementComment(
            @PathVariable Long commentId,
            @RequestBody ReportCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            log.info("📢 공지사항 댓글 신고 요청 - commentId: {}, user: {}", commentId, userDetails.getUsername());

            Long reportId = reportService.createAnnouncementCommentReport(
                    commentId,
                    userDetails.getUsername(),
                    request
            );

            log.info("✅ 공지사항 댓글 신고 성공 - reportId: {}", reportId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "reportId", reportId,
                    "message", "신고가 접수되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 공지사항 댓글 신고 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", "BAD_REQUEST",
                            "message", e.getMessage()
                    ));

        } catch (IllegalStateException e) {
            log.warn("⚠️ 공지사항 댓글 신고 실패 - 비즈니스 로직 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "success", false,
                            "error", "CONFLICT",
                            "message", e.getMessage()
                    ));

        } catch (Exception e) {
            log.error("❌ 공지사항 댓글 신고 실패 - 서버 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "INTERNAL_SERVER_ERROR",
                            "message", "신고 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                    ));
        }
    }

    /**
     * 전역 예외 처리 (Controller Advice 대신 간단한 핸들러)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception e) {
        log.error("❌ 예상치 못한 오류 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "success", false,
                        "error", "INTERNAL_SERVER_ERROR",
                        "message", "서버 오류가 발생했습니다."
                ));
    }
}