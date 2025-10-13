package com.example.game_backend.service;

import com.example.game_backend.controller.dto.NotificationDto;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.entity.Penalty;
import com.example.game_backend.repository.entity.report.*;
import java.util.List;

/**
 * 알림 시스템
 */
public interface NotificationService {

    /**
     * 신고 처리 완료 알림 (신고자에게)
     */
    void sendReportProcessedNotification(Member reporter, BaseReport report, ReportStatus status);

    /**
     * 제재 받음 알림 (피신고자에게)
     */
    void sendPenaltyNotification(Member reported, Penalty penalty);

    /**
     * 이의신청 접수 알림
     */
    void sendAppealReceivedNotification(Member member);

    /**
     * 이의신청 승인 알림
     */
    void sendAppealApprovedNotification(Member member);

    /**
     * 이의신청 거부 알림
     */
    void sendAppealRejectedNotification(Member member);

    /**
     * 누적 제재 자동 강화 알림
     */
    void sendAutoEscalationNotification(Member member);

    /**
     * 알림 읽음 처리
     */
    void markAsRead(Long notificationId, String username);

    /**
     * 모든 알림 읽음 처리
     */
    void markAllAsRead(String username);

    /**
     * 알림 목록 조회
     */
    List<NotificationDto> getNotifications(String username);

    /**
     * 읽지 않은 알림 개수
     */
    long getUnreadCount(String username);
}