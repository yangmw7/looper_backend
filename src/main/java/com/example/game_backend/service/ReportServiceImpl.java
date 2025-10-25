package com.example.game_backend.service;

import com.example.game_backend.controller.dto.report.*;
import com.example.game_backend.repository.*;
import com.example.game_backend.repository.entity.*;
import com.example.game_backend.repository.entity.report.*;
import com.example.game_backend.repository.report.*;
import com.example.game_backend.repository.report.AnnouncementCommentReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final AnnouncementCommentRepository announcementCommentRepository;
    private final MemberRepository memberRepository;
    private final PostReportRepository postReportRepository;
    private final CommentReportRepository commentReportRepository;
    private final AnnouncementCommentReportRepository announcementCommentReportRepository;

    private static final int DUP_MINUTES = 24 * 60; // 24시간

    // ===== 사용자: 게시글 신고 =====
    @Override
    public Long createPostReport(Long postId, String reporterUsername, ReportCreateRequest req) {
        try {
            log.info("📋 게시글 신고 시작 - postId: {}, reporter: {}", postId, reporterUsername);

            // 1. 게시글 조회
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> {
                        log.error("❌ 게시글 없음 - postId: {}", postId);
                        return new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + postId);
                    });

            // 2. 신고자 조회
            Member reporter = memberRepository.findByUsername(reporterUsername)
                    .orElseThrow(() -> {
                        log.error("❌ 회원 없음 - username: {}", reporterUsername);
                        return new IllegalArgumentException("회원을 찾을 수 없습니다: " + reporterUsername);
                    });

            Member reported = post.getWriter();

            // 3. 자기 자신 신고 방지
            if (reporter.getId().equals(reported.getId())) {
                log.warn("⚠️ 자기 자신 신고 시도 - userId: {}", reporter.getId());
                throw new IllegalStateException("자신의 게시글은 신고할 수 없습니다.");
            }

            // 4. 중복 신고 방지
            if (postReportRepository.existsByPost_IdAndReporter_IdAndCreatedAtAfter(
                    postId, reporter.getId(), LocalDateTime.now().minusMinutes(DUP_MINUTES))) {
                log.warn("⚠️ 중복 신고 시도 - postId: {}, userId: {}", postId, reporter.getId());
                throw new IllegalStateException("이미 신고하셨습니다. 24시간 후에 다시 시도해주세요.");
            }

            // 5. PostReport 생성
            PostReport report = PostReport.builder()
                    .post(post)
                    .build();

            report.setReporter(reporter);
            report.setReported(reported);

            // ⭐ reasons가 List<String>인 경우
            report.setReasons(req.reasons());
            report.setDescription(req.description());
            report.setStatus(ReportStatus.PENDING);

            // 6. 저장
            PostReport savedReport = postReportRepository.save(report);
            log.info("✅ 게시글 신고 완료 - reportId: {}", savedReport.getId());

            return savedReport.getId();

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("❌ 게시글 신고 실패 - 비즈니스 로직 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ 게시글 신고 실패 - 예상치 못한 오류", e);
            throw new RuntimeException("게시글 신고 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // ===== 사용자: 댓글 신고 =====
    @Override
    public Long createCommentReport(Long commentId, String reporterUsername, ReportCreateRequest req) {
        try {
            log.info("💬 댓글 신고 시작 - commentId: {}, reporter: {}", commentId, reporterUsername);

            // 1. 댓글 조회
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> {
                        log.error("❌ 댓글 없음 - commentId: {}", commentId);
                        return new IllegalArgumentException("댓글을 찾을 수 없습니다. ID: " + commentId);
                    });

            // 2. 신고자 조회
            Member reporter = memberRepository.findByUsername(reporterUsername)
                    .orElseThrow(() -> {
                        log.error("❌ 회원 없음 - username: {}", reporterUsername);
                        return new IllegalArgumentException("회원을 찾을 수 없습니다: " + reporterUsername);
                    });

            Member reported = comment.getMember();

            // 3. 자기 자신 신고 방지
            if (reporter.getId().equals(reported.getId())) {
                log.warn("⚠️ 자기 자신 신고 시도 - userId: {}", reporter.getId());
                throw new IllegalStateException("자신의 댓글은 신고할 수 없습니다.");
            }

            // 4. 중복 신고 방지
            if (commentReportRepository.existsByComment_IdAndReporter_IdAndCreatedAtAfter(
                    commentId, reporter.getId(), LocalDateTime.now().minusMinutes(DUP_MINUTES))) {
                log.warn("⚠️ 중복 신고 시도 - commentId: {}, userId: {}", commentId, reporter.getId());
                throw new IllegalStateException("이미 신고하셨습니다. 24시간 후에 다시 시도해주세요.");
            }

            // 5. CommentReport 생성
            CommentReport report = CommentReport.builder()
                    .comment(comment)
                    .build();

            report.setReporter(reporter);
            report.setReported(reported);

            // ⭐ reasons가 List<String>인 경우
            report.setReasons(req.reasons());
            report.setDescription(req.description());
            report.setStatus(ReportStatus.PENDING);

            // 6. 저장
            CommentReport savedReport = commentReportRepository.save(report);
            log.info("✅ 댓글 신고 완료 - reportId: {}", savedReport.getId());

            return savedReport.getId();

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("❌ 댓글 신고 실패 - 비즈니스 로직 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ 댓글 신고 실패 - 예상치 못한 오류", e);
            throw new RuntimeException("댓글 신고 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // ========== 🆕 사용자: 공지사항 댓글 신고 ==========
    @Override
    public Long createAnnouncementCommentReport(Long commentId, String reporterUsername, ReportCreateRequest req) {
        try {
            log.info("📢 공지사항 댓글 신고 시작 - commentId: {}, reporter: {}", commentId, reporterUsername);

            // 1. 댓글 조회
            AnnouncementComment comment = announcementCommentRepository.findById(commentId)
                    .orElseThrow(() -> {
                        log.error("❌ 공지사항 댓글 없음 - commentId: {}", commentId);
                        return new IllegalArgumentException("공지사항 댓글을 찾을 수 없습니다. ID: " + commentId);
                    });

            // 2. 신고자 조회
            Member reporter = memberRepository.findByUsername(reporterUsername)
                    .orElseThrow(() -> {
                        log.error("❌ 회원 없음 - username: {}", reporterUsername);
                        return new IllegalArgumentException("회원을 찾을 수 없습니다: " + reporterUsername);
                    });

            Member reported = comment.getMember();

            // 3. 자기 자신 신고 방지
            if (reporter.getId().equals(reported.getId())) {
                log.warn("⚠️ 자기 자신 신고 시도 - userId: {}", reporter.getId());
                throw new IllegalStateException("자신의 댓글은 신고할 수 없습니다.");
            }

            // 4. 중복 신고 방지
            if (announcementCommentReportRepository.existsByAnnouncementComment_IdAndReporter_IdAndCreatedAtAfter(
                    commentId, reporter.getId(), LocalDateTime.now().minusMinutes(DUP_MINUTES))) {
                log.warn("⚠️ 중복 신고 시도 - commentId: {}, userId: {}", commentId, reporter.getId());
                throw new IllegalStateException("이미 신고하셨습니다. 24시간 후에 다시 시도해주세요.");
            }

            // 5. AnnouncementCommentReport 생성
            AnnouncementCommentReport report = AnnouncementCommentReport.builder()
                    .announcementComment(comment)
                    .build();

            report.setReporter(reporter);
            report.setReported(reported);

            // ⭐ reasons가 List<String>인 경우
            report.setReasons(req.reasons());
            report.setDescription(req.description());
            report.setStatus(ReportStatus.PENDING);

            // 6. 저장
            AnnouncementCommentReport savedReport = announcementCommentReportRepository.save(report);
            log.info("✅ 공지사항 댓글 신고 완료 - reportId: {}", savedReport.getId());

            return savedReport.getId();

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("❌ 공지사항 댓글 신고 실패 - 비즈니스 로직 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ 공지사항 댓글 신고 실패 - 예상치 못한 오류", e);
            throw new RuntimeException("공지사항 댓글 신고 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // ===== 나머지 메서드는 기존과 동일 =====
    @Override
    @Transactional(readOnly = true)
    public Page<ReportDto> getPostReports(Set<ReportStatus> statuses, Pageable pageable) {
        Page<PostReport> page = (statuses == null || statuses.isEmpty())
                ? postReportRepository.findAll(pageable)
                : postReportRepository.findByStatusIn(statuses, pageable);
        return page.map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportDto> getCommentReports(Set<ReportStatus> statuses, Pageable pageable) {
        Page<CommentReport> page = (statuses == null || statuses.isEmpty())
                ? commentReportRepository.findAll(pageable)
                : commentReportRepository.findByStatusIn(statuses, pageable);
        return page.map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportDto> getAnnouncementCommentReports(Set<ReportStatus> statuses, Pageable pageable) {
        Page<AnnouncementCommentReport> page = (statuses == null || statuses.isEmpty())
                ? announcementCommentReportRepository.findAll(pageable)
                : announcementCommentReportRepository.findByStatusIn(statuses, pageable);
        return page.map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDto getPostReport(Long id) {
        PostReport report = postReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역을 찾을 수 없습니다."));
        return toDto(report);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDto getCommentReport(Long id) {
        CommentReport report = commentReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역을 찾을 수 없습니다."));
        return toDto(report);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDto getAnnouncementCommentReport(Long id) {
        AnnouncementCommentReport report = announcementCommentReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역을 찾을 수 없습니다."));
        return toDto(report);
    }

    private ReportDto toDto(PostReport r) {
        return new ReportDto(
                r.getId(),
                "POST",
                r.getPost().getId(),
                r.getPost().getTitle(),
                r.getPost().getContent(),
                r.getReporter().getId(),
                r.getReporter().getNickname(),
                r.getReported().getId(),
                r.getReported().getNickname(),
                r.getReasons(),
                r.getDescription(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getHandledBy(),
                r.getHandledAt(),
                r.getHandlerMemo()
        );
    }

    private ReportDto toDto(CommentReport r) {
        return new ReportDto(
                r.getId(),
                "COMMENT",
                r.getComment().getId(),
                null,
                r.getComment().getContent(),
                r.getReporter().getId(),
                r.getReporter().getNickname(),
                r.getReported().getId(),
                r.getReported().getNickname(),
                r.getReasons(),
                r.getDescription(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getHandledBy(),
                r.getHandledAt(),
                r.getHandlerMemo()
        );
    }

    private ReportDto toDto(AnnouncementCommentReport r) {
        return new ReportDto(
                r.getId(),
                "ANNOUNCEMENT_COMMENT",
                r.getAnnouncementComment().getId(),
                null,
                r.getAnnouncementComment().getContent(),
                r.getReporter().getId(),
                r.getReporter().getNickname(),
                r.getReported().getId(),
                r.getReported().getNickname(),
                r.getReasons(),
                r.getDescription(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getHandledBy(),
                r.getHandledAt(),
                r.getHandlerMemo()
        );
    }
}