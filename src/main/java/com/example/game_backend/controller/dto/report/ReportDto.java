package com.example.game_backend.controller.dto.report;

import com.example.game_backend.repository.entity.report.ReasonCode;
import com.example.game_backend.repository.entity.report.ReportStatus;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 관리자가 신고 목록 볼 때 사용 (상세 정보 포함)
 */
public record ReportDto(
        Long id,
        String reportType,           // POST or COMMENT
        Long targetId,               // 게시글 ID or 댓글 ID
        String targetTitle,          // 게시글 제목 (댓글은 null)
        String targetContent,        // 게시글/댓글 내용
        Long reporterId,
        String reporterNickname,
        Long reportedId,
        String reportedNickname,
        Set<ReasonCode> reasons,
        String description,
        ReportStatus status,
        LocalDateTime createdAt,
        String handledBy,
        LocalDateTime handledAt,
        String handlerMemo
) {}