package com.example.game_backend.service;

import com.example.game_backend.controller.dto.NotificationDto;
import com.example.game_backend.repository.*;
import com.example.game_backend.repository.entity.*;
import com.example.game_backend.repository.entity.report.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    // ===== 신고 처리 완료 알림 (신고자에게) =====
    @Override
    public void sendReportProcessedNotification(Member reporter, BaseReport report, ReportStatus status) {
        String title = "신고 처리 완료";
        String message = switch (status) {
            case REJECTED -> "신고하신 내용이 검토 후 기각되었습니다.";
            case RESOLVED -> "신고하신 내용이 확인되어 경고 조치되었습니다.";
            case ACTION_TAKEN -> "신고하신 내용이 확인되어 제재 조치되었습니다.";
            default -> "신고하신 내용이 처리되었습니다.";
        };

        Notification notification = Notification.builder()
                .recipient(reporter)
                .type(NotificationType.REPORT_PROCESSED)
                .title(title)
                .message(message)
                .linkUrl("/mypage?tab=reports")
                .build();

        notificationRepository.save(notification);
    }

    // ===== 제재 받음 알림 (피신고자에게) =====
    @Override
    public void sendPenaltyNotification(Member reported, Penalty penalty) {
        String title = switch (penalty.getType()) {
            case WARNING -> "경고 알림";
            case SUSPENSION -> "계정 정지 알림";
            case PERMANENT -> "영구정지 알림";
        };

        String message = String.format(
                "%s 제재를 받았습니다. 사유: %s",
                getPenaltyTypeKorean(penalty.getType()),
                penalty.getReason()
        );

        Notification notification = Notification.builder()
                .recipient(reported)
                .type(NotificationType.PENALTY_RECEIVED)
                .title(title)
                .message(message)
                .linkUrl("/mypage?tab=penalties")
                .build();

        notificationRepository.save(notification);
    }

    // ===== 이의신청 접수 알림 =====
    @Override
    public void sendAppealReceivedNotification(Member member) {
        Notification notification = Notification.builder()
                .recipient(member)
                .type(NotificationType.APPEAL_RECEIVED)
                .title("이의신청 접수 완료")
                .message("이의신청이 정상적으로 접수되었습니다. 검토까지 2-3일 소요될 수 있습니다.")
                .linkUrl("/mypage?tab=penalties")
                .build();

        notificationRepository.save(notification);
    }

    // ===== 이의신청 승인 알림 (NEW) =====
    @Override
    public void sendAppealApprovedNotification(Member member) {
        Notification notification = Notification.builder()
                .recipient(member)
                .type(NotificationType.APPEAL_APPROVED)
                .title("이의신청 승인")
                .message("이의신청이 승인되어 제재가 해제되었습니다.")
                .linkUrl("/mypage?tab=penalties")
                .build();

        notificationRepository.save(notification);
    }

    // ===== 이의신청 거부 알림 (NEW) =====
    @Override
    public void sendAppealRejectedNotification(Member member) {
        Notification notification = Notification.builder()
                .recipient(member)
                .type(NotificationType.APPEAL_REJECTED)
                .title("이의신청 기각")
                .message("이의신청이 기각되었습니다. 관리자 답변을 확인해주세요.")
                .linkUrl("/mypage?tab=penalties")
                .build();

        notificationRepository.save(notification);
    }

    // ===== 누적 제재 자동 강화 알림 =====
    @Override
    public void sendAutoEscalationNotification(Member member) {
        Notification notification = Notification.builder()
                .recipient(member)
                .type(NotificationType.AUTO_ESCALATION)
                .title("⚠️ 누적 제재 알림")
                .message("경고가 3회 누적되어 자동으로 3일 정지 조치되었습니다.")
                .linkUrl("/mypage?tab=penalties")
                .build();

        notificationRepository.save(notification);
    }

    // ===== 알림 읽음 처리 =====
    @Override
    public void markAsRead(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        if (!notification.getRecipient().getUsername().equals(username)) {
            throw new IllegalStateException("본인의 알림만 읽을 수 있습니다.");
        }

        notification.setIsRead(true);
    }

    // ===== 모든 알림 읽음 처리 =====
    @Override
    public void markAllAsRead(String username) {
        List<Notification> notifications = notificationRepository
                .findByRecipient_UsernameAndIsReadFalseOrderByCreatedAtDesc(username);

        notifications.forEach(n -> n.setIsRead(true));
    }

    // ===== 알림 목록 조회 =====
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications(String username) {
        return notificationRepository
                .findByRecipient_UsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(NotificationDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ===== 읽지 않은 알림 개수 =====
    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String username) {
        return notificationRepository.countByRecipient_UsernameAndIsReadFalse(username);
    }

    // ===== 제재 유형 한글 변환 =====
    private String getPenaltyTypeKorean(PenaltyType type) {
        return switch (type) {
            case WARNING -> "경고";
            case SUSPENSION -> "정지";
            case PERMANENT -> "영구정지";
        };
    }
}