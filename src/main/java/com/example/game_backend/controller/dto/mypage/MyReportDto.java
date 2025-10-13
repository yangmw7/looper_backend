package com.example.game_backend.controller.dto.mypage;

import com.example.game_backend.repository.entity.report.*;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 사용자가 자신이 신고한 내역 볼 때 사용
 */
public record MyReportDto(
        Long id,
        String reportType,           // POST or COMMENT
        String category,             // 신고 유형 (한글)
        String reason,               // 신고 사유 (한글)
        String description,
        String targetUser,           // 피신고자 닉네임
        String status,               // pending, completed, rejected
        String result,               // 처리 결과
        LocalDateTime createdAt
) {
    public static MyReportDto fromPostReport(PostReport report) {
        return new MyReportDto(
                report.getId(),
                "POST",
                convertReasonsToCategory(report.getReasons()),
                convertReasonsToString(report.getReasons()),
                report.getDescription(),
                report.getReported().getNickname(),
                convertStatus(report.getStatus()),
                report.getHandlerMemo(),
                report.getCreatedAt()
        );
    }

    public static MyReportDto fromCommentReport(CommentReport report) {
        return new MyReportDto(
                report.getId(),
                "COMMENT",
                convertReasonsToCategory(report.getReasons()),
                convertReasonsToString(report.getReasons()),
                report.getDescription(),
                report.getReported().getNickname(),
                convertStatus(report.getStatus()),
                report.getHandlerMemo(),
                report.getCreatedAt()
        );
    }

    private static String convertReasonsToCategory(Set<ReasonCode> reasons) {
        if (reasons == null || reasons.isEmpty()) return "기타";
        ReasonCode first = reasons.iterator().next();
        return switch (first) {
            case SPAM -> "스팸/광고";
            case ABUSE -> "욕설/비방";
            case HATE -> "혐오 발언";
            case SEXUAL -> "음란물";
            case ILLEGAL -> "불법 정보";
            case PERSONAL_INFO -> "개인정보 노출";
            case OTHER -> "기타";
        };
    }

    private static String convertReasonsToString(Set<ReasonCode> reasons) {
        if (reasons == null || reasons.isEmpty()) return "기타";
        return reasons.stream()
                .map(r -> switch (r) {
                    case SPAM -> "스팸/광고";
                    case ABUSE -> "욕설/비방";
                    case HATE -> "혐오 발언";
                    case SEXUAL -> "음란물";
                    case ILLEGAL -> "불법 정보";
                    case PERSONAL_INFO -> "개인정보 노출";
                    case OTHER -> "기타";
                })
                .reduce((a, b) -> a + ", " + b)
                .orElse("기타");
    }

    private static String convertStatus(ReportStatus status) {
        return switch (status) {
            case PENDING, IN_REVIEW -> "pending";
            case ACTION_TAKEN, RESOLVED -> "completed";
            case REJECTED -> "rejected";
        };
    }
}