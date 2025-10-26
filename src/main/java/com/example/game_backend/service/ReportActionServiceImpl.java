package com.example.game_backend.service;

import com.example.game_backend.controller.dto.report.ReportActionRequest;
import com.example.game_backend.repository.*;
import com.example.game_backend.repository.PenaltyRepository;
import com.example.game_backend.repository.entity.*;
import com.example.game_backend.repository.entity.report.*;
import com.example.game_backend.repository.report.*;
import com.example.game_backend.repository.report.AnnouncementCommentReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
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
        // 필수 값 검증
        if (req.penaltyType() == null) {
            log.error("제재 유형이 누락됨");
            throw new IllegalArgumentException("제재 유형을 선택해주세요.");
        }

        if (req.penaltyReason() == null || req.penaltyReason().isBlank()) {
            log.error("제재 사유가 누락됨");
            throw new IllegalArgumentException("제재 사유를 입력해주세요.");
        }

        if (req.penaltyReason().length() < 5) {
            log.error("제재 사유가 너무 짧음: {}", req.penaltyReason());
            throw new IllegalArgumentException("제재 사유는 최소 5자 이상 입력해주세요.");
        }

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = null;

        if (req.penaltyType() == PenaltyType.SUSPENSION) {
            if (req.suspensionDays() == null || req.suspensionDays() <= 0) {
                log.error("정지 기간이 누락되거나 잘못됨: {}", req.suspensionDays());
                throw new IllegalArgumentException("정지 기간을 올바르게 입력해주세요. (1일 이상)");
            }
            endDate = startDate.plusDays(req.suspensionDays());
        }

        try {
            Penalty penalty = Penalty.builder()
                    .member(member)
                    .type(req.penaltyType())
                    .reason(req.penaltyReason())
                    .description(req.penaltyDescription() != null ? req.penaltyDescription() : "")
                    .evidence(req.evidence())
                    .startDate(startDate)
                    .endDate(endDate)
                    .isActive(true)
                    .canAppeal(true)
                    .appealSubmitted(false)
                    .issuedBy(adminUsername)
                    .build();

            Penalty savedPenalty = penaltyRepository.save(penalty);
            log.info("제재 생성 완료 - penaltyId: {}, memberId: {}, type: {}",
                    savedPenalty.getId(), member.getId(), req.penaltyType());

            return savedPenalty;

        } catch (Exception e) {
            log.error("제재 생성 중 오류 발생", e);
            throw new RuntimeException("제재 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private void updateReportedUserStats(Member member, PenaltyType penaltyType) {
        try {
            member.setReportCount(member.getReportCount() + 1);

            switch (penaltyType) {
                case WARNING -> {
                    member.setWarningCount(member.getWarningCount() + 1);
                    log.info("경고 카운트 증가 - memberId: {}, count: {}",
                            member.getId(), member.getWarningCount());
                }
                case SUSPENSION -> {
                    member.setSuspensionCount(member.getSuspensionCount() + 1);
                    log.info("정지 카운트 증가 - memberId: {}, count: {}",
                            member.getId(), member.getSuspensionCount());
                }
                case PERMANENT -> {
                    member.setEnabled(false);
                    log.info("영구정지 처리 - memberId: {}", member.getId());
                }
            }

            memberRepository.save(member);
            checkAutoEscalation(member);

        } catch (Exception e) {
            log.error("회원 통계 업데이트 중 오류 발생 - memberId: {}", member.getId(), e);
            throw new RuntimeException("회원 통계 업데이트 중 오류가 발생했습니다.", e);
        }
    }

    private void checkAutoEscalation(Member member) {
        try {
            // 경고 3회 누적 시 자동 정지
            if (member.getWarningCount() >= 3 && member.getSuspensionCount() == 0) {
                log.warn("자동 정지 처분 발동 - memberId: {}, warningCount: {}",
                        member.getId(), member.getWarningCount());

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

                log.info("자동 정지 처분 완료 - memberId: {}", member.getId());
            }

            // 정지 3회 누적 시 영구정지
            if (member.getSuspensionCount() >= 3) {
                log.warn("자동 영구정지 처분 발동 - memberId: {}, suspensionCount: {}",
                        member.getId(), member.getSuspensionCount());

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

                log.info("자동 영구정지 처분 완료 - memberId: {}", member.getId());
            }

        } catch (Exception e) {
            log.error("자동 누적 처리 중 오류 발생 - memberId: {}", member.getId(), e);
            // 자동 처리 실패는 전체 트랜잭션을 롤백하지 않도록 예외를 로깅만 함
        }
    }
}