package com.example.game_backend.repository.entity.report;

public enum ReportStatus {
    PENDING,        // 접수
    IN_REVIEW,      // 확인 중
    REJECTED,       // 기각
    ACTION_TAKEN,   // 조치 완료(제재 등)
    RESOLVED        // 종결(경고 등 경미 조치 포함)
}