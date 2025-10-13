package com.example.game_backend.controller.dto.mypage;

import com.example.game_backend.repository.entity.Appeal;
import com.example.game_backend.repository.entity.AppealStatus;

import java.time.LocalDateTime;

public record AppealDto(
        Long id,
        Long penaltyId,
        String appealReason,
        AppealStatus status,
        String adminResponse,
        String processedBy,
        LocalDateTime processedAt,
        LocalDateTime createdAt
) {
    public static AppealDto fromEntity(Appeal appeal) {
        return new AppealDto(
                appeal.getId(),
                appeal.getPenalty().getId(),
                appeal.getAppealReason(),
                appeal.getStatus(),
                appeal.getAdminResponse(),
                appeal.getProcessedBy(),
                appeal.getProcessedAt(),
                appeal.getCreatedAt()
        );
    }
}