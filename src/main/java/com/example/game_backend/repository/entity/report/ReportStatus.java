package com.example.game_backend.repository.entity.report;

public enum ReportStatus {
    PENDING,        // 접수됨
    IN_REVIEW,      // 검토 중
    REJECTED,       // 기각됨
    ACTION_TAKEN,   // 제재 조치 완료
    RESOLVED        // 처리 완료 (경고 등)
}