package com.example.game_backend.controller.dto.report;

import com.example.game_backend.repository.entity.report.ReasonCode;

import java.util.Set;

/**
 * 사용자가 신고할 때 보내는 요청
 */
public record ReportCreateRequest(
        Set<ReasonCode> reasons,      // 신고 사유들
        String description            // 상세 설명
) {}