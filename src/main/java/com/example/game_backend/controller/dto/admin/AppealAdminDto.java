package com.example.game_backend.controller.dto.admin;

import com.example.game_backend.repository.entity.Appeal;
import com.example.game_backend.repository.entity.AppealStatus;
import com.example.game_backend.repository.entity.PenaltyType;

import java.time.LocalDateTime;

public record AppealAdminDto(
        Long id,
        Long penaltyId,
        String memberUsername,
        String memberNickname,
        PenaltyType penaltyType,
        String penaltyReason,
        LocalDateTime penaltyStartDate,
        LocalDateTime penaltyEndDate,
        String appealReason,
        AppealStatus status,
        String adminResponse,
        String processedBy,
        LocalDateTime processedAt,
        LocalDateTime createdAt
) {
    public static AppealAdminDto fromEntity(Appeal appeal) {
        return new AppealAdminDto(
                appeal.getId(),
                appeal.getPenalty().getId(),
                appeal.getPenalty().getMember().getUsername(),
                appeal.getPenalty().getMember().getNickname(),
                appeal.getPenalty().getType(),
                appeal.getPenalty().getReason(),
                appeal.getPenalty().getStartDate(),
                appeal.getPenalty().getEndDate(),
                appeal.getAppealReason(),
                appeal.getStatus(),
                appeal.getAdminResponse(),
                appeal.getProcessedBy(),
                appeal.getProcessedAt(),
                appeal.getCreatedAt()
        );
    }
}