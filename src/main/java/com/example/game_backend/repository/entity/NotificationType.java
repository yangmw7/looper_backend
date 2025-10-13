package com.example.game_backend.repository.entity;

public enum NotificationType {
    REPORT_PROCESSED,    // 신고 처리 완료 (신고자에게)
    PENALTY_RECEIVED,    // 제재 받음 (피신고자에게)
    APPEAL_RECEIVED,     // 이의신청 접수됨
    APPEAL_APPROVED,     // 이의신청 승인됨
    APPEAL_REJECTED,     // 이의신청 기각됨
    AUTO_ESCALATION      // 자동 제재 강화
}