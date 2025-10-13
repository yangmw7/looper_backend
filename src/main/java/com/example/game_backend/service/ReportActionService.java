package com.example.game_backend.service;

import com.example.game_backend.controller.dto.report.ReportActionRequest;

/**
 * 관리자가 신고 처리 + 제재 부과
 */
public interface ReportActionService {

    /**
     * 게시글 신고 처리 (제재 포함)
     */
    void processPostReport(Long reportId, String adminUsername, ReportActionRequest req);

    /**
     * 댓글 신고 처리 (제재 포함)
     */
    void processCommentReport(Long reportId, String adminUsername, ReportActionRequest req);
}