package com.example.game_backend.service;

import com.example.game_backend.controller.dto.report.ReportActionRequest;
import com.example.game_backend.repository.*;
import com.example.game_backend.repository.PenaltyRepository;
import com.example.game_backend.repository.entity.*;
import com.example.game_backend.repository.entity.report.*;
import com.example.game_backend.repository.report.*;
import com.example.game_backend.repository.report.AnnouncementCommentReportRepository;
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
    private final AnnouncementCommentReportRepository announcementCommentReportRepository;
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

    // ========== 공지사항 댓글 신고 처리 ==========
    @Override
    public void processAnnouncementCommentReport(Long reportId, String adminUsername, ReportActionRequest req) {
        AnnouncementCommentReport report = announcementCommentReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역을 찾을 수 없습니다."));

        processReport(report, report.getReporter(), report.getReported(), adminUsername, req);
    }

    // ===== 공통 처리 로직 =====
    private void processReport(BaseReport report, Member reporter, Member reported,
                               String adminUsername, ReportActionRequest req) {

        report.setStatus(req.status());
        report.setHandledBy(adminUsername);
        report.setHandledAt(LocalDateTime.now());
        report.setHandlerMemo(req.handlerMemo());

        notificationService.sendReportProcessedNotification(reporter, report, req.status());

        if ((req.status() == ReportStatus.ACTION_TAKEN || req.status() == ReportStatus.RESOLVED)
                && req.penaltyType() != null) {

            Penalty penalty = createPenalty(reported, adminUsername, req);
            updateReportedUserStats(reported, req.penaltyType());
            notificationService.sendPenaltyNotification(reported, penalty);
        }
    }

    private Penalty createPenalty(Member member, String adminUsername, ReportActionRequest req) {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = null;

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

    private void updateReportedUserStats(Member member, PenaltyType penaltyType) {
        member.setReportCount(member.getReportCount() + 1);

        switch (penaltyType) {
            case WARNING -> member.setWarningCount(member.getWarningCount() + 1);
            case SUSPENSION -> member.setSuspensionCount(member.getSuspensionCount() + 1);
            case PERMANENT -> member.setEnabled(false);
        }

        memberRepository.save(member);
        checkAutoEscalation(member);
    }

    private void checkAutoEscalation(Member member) {
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