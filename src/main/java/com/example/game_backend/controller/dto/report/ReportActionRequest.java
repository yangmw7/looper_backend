package com.example.game_backend.controller.dto.report;

import com.example.game_backend.repository.entity.PenaltyType;
import com.example.game_backend.repository.entity.report.ReportStatus;

/**
 * 관리자가 신고를 처리할 때 보내는 요청
 */
public record ReportActionRequest(
        ReportStatus status,          // REJECTED, ACTION_TAKEN, RESOLVED
        String handlerMemo,           // 처리 메모

        // 제재 부과 시에만 필요
        PenaltyType penaltyType,      // WARNING, SUSPENSION, PERMANENT
        String penaltyReason,         // 제재 사유
        String penaltyDescription,    // 제재 상세 설명
        Integer suspensionDays,       // 정지 일수 (SUSPENSION일 때만)
        String evidence               // 증거 자료
) {}