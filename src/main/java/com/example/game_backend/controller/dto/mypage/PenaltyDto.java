package com.example.game_backend.controller.dto.mypage;

import com.example.game_backend.repository.entity.Penalty;
import java.time.LocalDateTime;

/**
 * 사용자가 자신의 제재 내역 볼 때 사용
 */
public record PenaltyDto(
        Long id,
        String type,                 // warning, suspension, permanent
        String reason,
        String description,
        String evidence,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Boolean isActive,
        Boolean canAppeal,
        Boolean appealSubmitted,
        LocalDateTime createdAt
) {
    public static PenaltyDto fromEntity(Penalty penalty) {
        return new PenaltyDto(
                penalty.getId(),
                penalty.getType().name().toLowerCase(),
                penalty.getReason(),
                penalty.getDescription(),
                penalty.getEvidence(),
                penalty.getStartDate(),
                penalty.getEndDate(),
                penalty.getIsActive(),
                penalty.getCanAppeal(),
                penalty.getAppealSubmitted(),
                penalty.getCreatedAt()
        );
    }

}