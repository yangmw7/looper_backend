package com.example.game_backend.service;

import com.example.game_backend.controller.dto.report.*;
import com.example.game_backend.repository.*;
import com.example.game_backend.repository.entity.*;
import com.example.game_backend.repository.entity.report.*;
import com.example.game_backend.repository.report.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostReportRepository postReportRepository;
    private final CommentReportRepository commentReportRepository;

    private static final int DUP_MINUTES = 24 * 60; // 24시간

    // ===== 사용자: 게시글 신고 =====
    @Override
    public Long createPostReport(Long postId, String reporterUsername, ReportCreateRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Member reporter = memberRepository.findByUsername(reporterUsername)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Member reported = post.getWriter();

        // 1. 자기 자신 신고 방지
        if (reporter.getId().equals(reported.getId())) {
            throw new IllegalStateException("자신의 게시글은 신고할 수 없습니다.");
        }

        // 2. 중복 신고 방지 (24시간 이내)
        if (postReportRepository.existsByPost_IdAndReporter_IdAndCreatedAtAfter(
                postId, reporter.getId(), LocalDateTime.now().minusMinutes(DUP_MINUTES))) {
            throw new IllegalStateException("이미 신고하셨습니다. 24시간 후에 다시 시도해주세요.");
        }

        // 3. 신고 생성
        PostReport report = PostReport.builder()
                .post(post)
                .build();

        report.setReporter(reporter);
        report.setReported(reported);
        report.setReasons(req.reasons());
        report.setDescription(req.description());
        report.setStatus(ReportStatus.PENDING);

        return postReportRepository.save(report).getId();
    }

    // ===== 사용자: 댓글 신고 =====
    @Override
    public Long createCommentReport(Long commentId, String reporterUsername, ReportCreateRequest req) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        Member reporter = memberRepository.findByUsername(reporterUsername)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Member reported = comment.getMember();

        // 1. 자기 자신 신고 방지
        if (reporter.getId().equals(reported.getId())) {
            throw new IllegalStateException("자신의 댓글은 신고할 수 없습니다.");
        }

        // 2. 중복 신고 방지 (24시간 이내)
        if (commentReportRepository.existsByComment_IdAndReporter_IdAndCreatedAtAfter(
                commentId, reporter.getId(), LocalDateTime.now().minusMinutes(DUP_MINUTES))) {
            throw new IllegalStateException("이미 신고하셨습니다. 24시간 후에 다시 시도해주세요.");
        }

        // 3. 신고 생성
        CommentReport report = CommentReport.builder()
                .comment(comment)
                .build();

        report.setReporter(reporter);
        report.setReported(reported);
        report.setReasons(req.reasons());
        report.setDescription(req.description());
        report.setStatus(ReportStatus.PENDING);

        return commentReportRepository.save(report).getId();
    }

    // ===== 관리자: 게시글 신고 목록 조회 =====
    @Override
    @Transactional(readOnly = true)
    public Page<ReportDto> getPostReports(Set<ReportStatus> statuses, Pageable pageable) {
        Page<PostReport> page = (statuses == null || statuses.isEmpty())
                ? postReportRepository.findAll(pageable)
                : postReportRepository.findByStatusIn(statuses, pageable);

        return page.map(this::toDto);
    }

    // ===== 관리자: 댓글 신고 목록 조회 =====
    @Override
    @Transactional(readOnly = true)
    public Page<ReportDto> getCommentReports(Set<ReportStatus> statuses, Pageable pageable) {
        Page<CommentReport> page = (statuses == null || statuses.isEmpty())
                ? commentReportRepository.findAll(pageable)
                : commentReportRepository.findByStatusIn(statuses, pageable);

        return page.map(this::toDto);
    }

    // ===== 관리자: 게시글 신고 상세 조회 =====
    @Override
    @Transactional(readOnly = true)
    public ReportDto getPostReport(Long id) {
        PostReport report = postReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역을 찾을 수 없습니다."));
        return toDto(report);
    }

    // ===== 관리자: 댓글 신고 상세 조회 =====
    @Override
    @Transactional(readOnly = true)
    public ReportDto getCommentReport(Long id) {
        CommentReport report = commentReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역을 찾을 수 없습니다."));
        return toDto(report);
    }

    // ===== DTO 변환 =====
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
                null, // 댓글은 제목 없음
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
}