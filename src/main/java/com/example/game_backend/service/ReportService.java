package com.example.game_backend.service;

import com.example.game_backend.controller.dto.report.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.game_backend.repository.entity.report.ReportStatus;
import java.util.Set;

/**
 * 사용자가 신고 제출 & 관리자가 신고 목록 조회
 */
public interface ReportService {

    // ===== 사용자: 신고 제출 =====
    Long createPostReport(Long postId, String reporterUsername, ReportCreateRequest req);
    Long createCommentReport(Long commentId, String reporterUsername, ReportCreateRequest req);

    // ===== 관리자: 신고 목록 조회 =====
    Page<ReportDto> getPostReports(Set<ReportStatus> statuses, Pageable pageable);
    Page<ReportDto> getCommentReports(Set<ReportStatus> statuses, Pageable pageable);
    ReportDto getPostReport(Long id);
    ReportDto getCommentReport(Long id);

    Long createAnnouncementCommentReport(Long commentId, String reporterUsername, ReportCreateRequest req);
    Page<ReportDto> getAnnouncementCommentReports(Set<ReportStatus> statuses, Pageable pageable);
    ReportDto getAnnouncementCommentReport(Long id);
}