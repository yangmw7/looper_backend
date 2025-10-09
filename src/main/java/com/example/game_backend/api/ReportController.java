// src/main/java/com/example/game_backend/api/ReportController.java
package com.example.game_backend.api;

import com.example.game_backend.controller.dto.report.*;
import com.example.game_backend.repository.entity.report.ReportStatus;
import com.example.game_backend.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // ========== 사용자 신고 ==========

    @PostMapping("/reports/posts/{postId}")
    @PreAuthorize("isAuthenticated()")
    public Map<String,Object> reportPost(
            @PathVariable Long postId,
            @Valid @RequestBody ReportCreateRequest req,
            @AuthenticationPrincipal String username) {

        Long id = reportService.createPostReport(postId, username, req);
        return Map.of("reportId", id, "message", "게시글 신고 접수 완료");
    }

    @PostMapping("/reports/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public Map<String,Object> reportComment(
            @PathVariable Long commentId,
            @Valid @RequestBody ReportCreateRequest req,
            @AuthenticationPrincipal String username) {

        Long id = reportService.createCommentReport(commentId, username, req);
        return Map.of("reportId", id, "message", "댓글 신고 접수 완료");
    }

    // ========== 관리자용 ==========

    @GetMapping("/admin/reports/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ReportDto> getPostReports(
            @RequestParam(required = false) Set<ReportStatus> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        Pageable pageable = PageRequest.of(page, size, toSort(sort));
        return reportService.getPostReports(status, pageable);
    }

    @GetMapping("/admin/reports/comments")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ReportDto> getCommentReports(
            @RequestParam(required = false) Set<ReportStatus> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        Pageable pageable = PageRequest.of(page, size, toSort(sort));
        return reportService.getCommentReports(status, pageable);
    }

    @GetMapping("/admin/reports/posts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ReportDto getPostReport(@PathVariable Long id) {
        return reportService.getPostReport(id);
    }

    @GetMapping("/admin/reports/comments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ReportDto getCommentReport(@PathVariable Long id) {
        return reportService.getCommentReport(id);
    }

    @PatchMapping("/admin/reports/posts/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String,String> updatePostReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportStatusUpdateRequest req,
            @AuthenticationPrincipal String adminUsername) {
        reportService.updatePostReportStatus(id, adminUsername, req);
        return Map.of("message", "게시글 신고 상태가 변경되었습니다.");
    }

    @PatchMapping("/admin/reports/comments/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String,String> updateCommentReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportStatusUpdateRequest req,
            @AuthenticationPrincipal String adminUsername) {
        reportService.updateCommentReportStatus(id, adminUsername, req);
        return Map.of("message", "댓글 신고 상태가 변경되었습니다.");
    }

    // ── 정렬 파라미터 파싱 ──
    private Sort toSort(String sortParam) {
        String[] p = sortParam.split(",");
        if (p.length == 2) {
            return Sort.by("desc".equalsIgnoreCase(p[1]) ? Sort.Direction.DESC : Sort.Direction.ASC, p[0]);
        }
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }
}
