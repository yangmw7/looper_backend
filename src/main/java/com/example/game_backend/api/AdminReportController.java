package com.example.game_backend.api;

import com.example.game_backend.controller.dto.report.*;
import com.example.game_backend.repository.entity.report.ReportStatus;
import com.example.game_backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final ReportService reportService;
    private final ReportActionService reportActionService;

    // ========== 신고 목록 조회 ==========

    /**
     * 게시글 신고 목록 조회
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<ReportDto>> getPostReports(
            @RequestParam(required = false) Set<ReportStatus> statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReportDto> reports = reportService.getPostReports(statuses, pageable);

        return ResponseEntity.ok(reports);
    }

    /**
     * 댓글 신고 목록 조회
     */
    @GetMapping("/comments")
    public ResponseEntity<Page<ReportDto>> getCommentReports(
            @RequestParam(required = false) Set<ReportStatus> statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReportDto> reports = reportService.getCommentReports(statuses, pageable);

        return ResponseEntity.ok(reports);
    }

    // ========== 신고 상세 조회 ==========

    /**
     * 게시글 신고 상세 조회
     */
    @GetMapping("/posts/{id}")
    public ResponseEntity<ReportDto> getPostReport(@PathVariable Long id) {
        ReportDto report = reportService.getPostReport(id);
        return ResponseEntity.ok(report);
    }

    /**
     * 댓글 신고 상세 조회
     */
    @GetMapping("/comments/{id}")
    public ResponseEntity<ReportDto> getCommentReport(@PathVariable Long id) {
        ReportDto report = reportService.getCommentReport(id);
        return ResponseEntity.ok(report);
    }

    // ========== 신고 처리 (제재 부과 포함) ==========

    /**
     * 게시글 신고 처리
     */
    @PostMapping("/posts/{id}/process")
    public ResponseEntity<Void> processPostReport(
            @PathVariable Long id,
            @RequestBody ReportActionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        reportActionService.processPostReport(
                id,
                userDetails.getUsername(),
                request
        );

        return ResponseEntity.ok().build();
    }

    /**
     * 댓글 신고 처리
     */
    @PostMapping("/comments/{id}/process")
    public ResponseEntity<Void> processCommentReport(
            @PathVariable Long id,
            @RequestBody ReportActionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        reportActionService.processCommentReport(
                id,
                userDetails.getUsername(),
                request
        );

        return ResponseEntity.ok().build();
    }
}