package com.example.game_backend.controller.dto;

import com.example.game_backend.repository.entity.Notification;
import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        String type,
        String title,
        String message,
        String linkUrl,
        Boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationDto fromEntity(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getType().name().toLowerCase(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getLinkUrl(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}