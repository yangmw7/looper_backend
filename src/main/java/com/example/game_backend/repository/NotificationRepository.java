package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 특정 회원의 모든 알림 (최신순)
    List<Notification> findByRecipient_UsernameOrderByCreatedAtDesc(String username);

    // 특정 회원의 읽지 않은 알림만
    List<Notification> findByRecipient_UsernameAndIsReadFalseOrderByCreatedAtDesc(String username);

    // 읽지 않은 알림 개수
    long countByRecipient_UsernameAndIsReadFalse(String username);
}