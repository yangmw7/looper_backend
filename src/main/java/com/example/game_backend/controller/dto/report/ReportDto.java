package com.example.game_backend.controller.dto.report;

import com.example.game_backend.repository.entity.report.ReasonCode;
import com.example.game_backend.repository.entity.report.ReportStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record ReportDto(
        Long id,
        String type,
        Long targetId,
        String targetTitle,      // 게시글 제목 (댓글은 null)
        String targetContent,    // 게시글 내용 or 댓글 내용 전체
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
