package com.example.game_backend.service;

import com.example.game_backend.controller.dto.report.ReportActionRequest;
import com.example.game_backend.repository.*;
import com.example.game_backend.repository.PenaltyRepository;
import com.example.game_backend.repository.entity.*;
import com.example.game_backend.repository.entity.report.*;
import com.example.game_backend.repository.report.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportActionServiceImpl implements ReportActionService {

    private final PostReportRepository postReportRepository;
    private final CommentReportRepository commentReportRepository;
    private final PenaltyRepository penaltyRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    // ===== 게시글 신고 처리 =====
    @Override
    public void processPostReport(Long reportId, String adminUsername, ReportActionRequest req) {
        PostReport report = postReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역을 찾을 수 없습니다."));

        processReport(report, report.getReporter(), report.getReported(), adminUsername, req);
    }

    // ===== 댓글 신고 처리 =====
    @Override
    public void processCommentReport(Long reportId, String adminUsername, ReportActionRequest req) {
        CommentReport report = commentReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역을 찾을 수 없습니다."));

        processReport(report, report.getReporter(), report.getReported(), adminUsername, req);
    }

    // ===== 공통 처리 로직 =====
    private void processReport(BaseReport report, Member reporter, Member reported,
                               String adminUsername, ReportActionRequest req) {

        // 1. 신고 상태 업데이트
        report.setStatus(req.status());
        report.setHandledBy(adminUsername);
        report.setHandledAt(LocalDateTime.now());
        report.setHandlerMemo(req.handlerMemo());

        // 2. 신고자에게 처리 완료 알림 (항상 발송)
        notificationService.sendReportProcessedNotification(reporter, report, req.status());

        // 3. 제재 부과 필요 시 처리
        if ((req.status() == ReportStatus.ACTION_TAKEN || req.status() == ReportStatus.RESOLVED)
                && req.penaltyType() != null) {

            // 제재 생성
            Penalty penalty = createPenalty(reported, adminUsername, req);

            // 피신고자 통계 업데이트
            updateReportedUserStats(reported, req.penaltyType());

            // 피신고자에게 제재 알림
            notificationService.sendPenaltyNotification(reported, penalty);
        }

        // 4. 기각(REJECTED)인 경우 피신고자에게는 알림 X
    }

    // ===== 제재 생성 =====
    private Penalty createPenalty(Member member, String adminUsername, ReportActionRequest req) {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = null;

        // 정지 기간 계산
        if (req.penaltyType() == PenaltyType.SUSPENSION && req.suspensionDays() != null) {
            endDate = startDate.plusDays(req.suspensionDays());
        }

        Penalty penalty = Penalty.builder()
                .member(member)
                .type(req.penaltyType())
                .reason(req.penaltyReason())
                .description(req.penaltyDescription())
                .evidence(req.evidence())
                .startDate(startDate)
                .endDate(endDate)
                .isActive(true)
                .canAppeal(true)
                .appealSubmitted(false)
                .issuedBy(adminUsername)
                .build();

        return penaltyRepository.save(penalty);
    }

    // ===== 피신고자 통계 업데이트 =====
    private void updateReportedUserStats(Member member, PenaltyType penaltyType) {
        // 신고 횟수 증가
        member.setReportCount(member.getReportCount() + 1);

        // 제재 유형별 누적
        switch (penaltyType) {
            case WARNING -> member.setWarningCount(member.getWarningCount() + 1);
            case SUSPENSION -> member.setSuspensionCount(member.getSuspensionCount() + 1);
            case PERMANENT -> member.setEnabled(false); // 영구정지는 계정 비활성화
        }

        memberRepository.save(member);

        // 누적 제재 자동 강화 체크
        checkAutoEscalation(member);
    }

    // ===== 누적 제재 자동 강화 =====
    private void checkAutoEscalation(Member member) {

        // 경고 3회 누적 → 자동 3일 정지
        if (member.getWarningCount() >= 3 && member.getSuspensionCount() == 0) {
            Penalty autoSuspension = Penalty.builder()
                    .member(member)
                    .type(PenaltyType.SUSPENSION)
                    .reason("경고 3회 누적으로 인한 자동 정지")
                    .description("경고가 3회 누적되어 자동으로 3일 정지 처분됩니다.")
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now().plusDays(3))
                    .isActive(true)
                    .canAppeal(true)
                    .issuedBy("SYSTEM")
                    .build();

            penaltyRepository.save(autoSuspension);
            member.setSuspensionCount(member.getSuspensionCount() + 1);
            memberRepository.save(member);

            notificationService.sendAutoEscalationNotification(member);
        }

        // 정지 3회 누적 → 영구정지
        if (member.getSuspensionCount() >= 3) {
            Penalty autoPermanent = Penalty.builder()
                    .member(member)
                    .type(PenaltyType.PERMANENT)
                    .reason("정지 3회 누적으로 인한 영구정지")
                    .description("정지 처분이 3회 누적되어 영구정지 처분됩니다.")
                    .startDate(LocalDateTime.now())
                    .endDate(null)
                    .isActive(true)
                    .canAppeal(false)
                    .issuedBy("SYSTEM")
                    .build();

            penaltyRepository.save(autoPermanent);
            member.setEnabled(false);
            memberRepository.save(member);
        }
    }
}