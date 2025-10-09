package com.example.game_backend.service;

import com.example.game_backend.controller.dto.report.ReportCreateRequest;
import com.example.game_backend.controller.dto.report.ReportDto;
import com.example.game_backend.controller.dto.report.ReportStatusUpdateRequest;
import com.example.game_backend.repository.entity.report.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface ReportService {
    Long createPostReport(Long postId, String reporterUsername, ReportCreateRequest req);
    Long createCommentReport(Long commentId, String reporterUsername, ReportCreateRequest req);

    Page<ReportDto> getPostReports(Set<ReportStatus> statuses, Pageable pageable);
    Page<ReportDto> getCommentReports(Set<ReportStatus> statuses, Pageable pageable);
    ReportDto getPostReport(Long reportId);
    ReportDto getCommentReport(Long reportId);

    void updatePostReportStatus(Long reportId, String adminUsername, ReportStatusUpdateRequest req);
    void updateCommentReportStatus(Long reportId, String adminUsername, ReportStatusUpdateRequest req);
}
