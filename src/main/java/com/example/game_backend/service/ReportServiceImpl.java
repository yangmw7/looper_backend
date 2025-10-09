package com.example.game_backend.service;

import com.example.game_backend.controller.dto.report.*;
import com.example.game_backend.repository.*;
import com.example.game_backend.repository.entity.*;
import com.example.game_backend.repository.entity.report.*;
import com.example.game_backend.repository.report.CommentReportRepository;
import com.example.game_backend.repository.report.PostReportRepository;
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

    @Override
    public Long createPostReport(Long postId, String reporterUsername, ReportCreateRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        Member reporter = memberRepository.findByUsername(reporterUsername)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));
        Member reported = post.getWriter();

        // 자기 자신 신고 방지
        if (reporter.getId().equals(reported.getId())) {
            throw new IllegalStateException("자신의 글은 신고할 수 없습니다.");
        }

        // 중복 신고 방지
        if (postReportRepository.existsByPost_IdAndReporter_IdAndCreatedAtAfter(
                postId, reporter.getId(), LocalDateTime.now().minusMinutes(DUP_MINUTES))) {
            throw new IllegalStateException("이미 신고했습니다. 일정 시간이 지난 후 다시 시도하세요.");
        }

        PostReport report = PostReport.builder().post(post).build();
        report.setReporter(reporter);
        report.setReported(reported);
        report.setReasons(req.reasons());
        report.setDescription(req.description());
        report.setStatus(ReportStatus.PENDING);
        return postReportRepository.save(report).getId();
    }

    @Override
    public Long createCommentReport(Long commentId, String reporterUsername, ReportCreateRequest req) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 없음"));
        Member reporter = memberRepository.findByUsername(reporterUsername)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));
        Member reported = comment.getMember();

        // 자기 자신 신고 방지
        if (reporter.getId().equals(reported.getId())) {
            throw new IllegalStateException("자신의 댓글은 신고할 수 없습니다.");
        }

        // 중복 신고 방지
        if (commentReportRepository.existsByComment_IdAndReporter_IdAndCreatedAtAfter(
                commentId, reporter.getId(), LocalDateTime.now().minusMinutes(DUP_MINUTES))) {
            throw new IllegalStateException("이미 신고했습니다. 일정 시간이 지난 후 다시 시도하세요.");
        }

        CommentReport report = CommentReport.builder().comment(comment).build();
        report.setReporter(reporter);
        report.setReported(reported);
        report.setReasons(req.reasons());
        report.setDescription(req.description());
        report.setStatus(ReportStatus.PENDING);
        return commentReportRepository.save(report).getId();
    }

    @Transactional(readOnly = true)
    public Page<ReportDto> getPostReports(Set<ReportStatus> statuses, Pageable pageable) {
        Page<PostReport> page = (statuses == null || statuses.isEmpty())
                ? postReportRepository.findAll(pageable)
                : postReportRepository.findByStatusIn(statuses, pageable);
        return page.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ReportDto> getCommentReports(Set<ReportStatus> statuses, Pageable pageable) {
        Page<CommentReport> page = (statuses == null || statuses.isEmpty())
                ? commentReportRepository.findAll(pageable)
                : commentReportRepository.findByStatusIn(statuses, pageable);
        return page.map(this::toDto);
    }

    public ReportDto getPostReport(Long id) {
        return toDto(postReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역 없음")));
    }

    public ReportDto getCommentReport(Long id) {
        return toDto(commentReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역 없음")));
    }

    public void updatePostReportStatus(Long id, String adminUsername, ReportStatusUpdateRequest req) {
        PostReport r = postReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역 없음"));
        r.setStatus(req.status());
        r.setHandledBy(adminUsername);
        r.setHandledAt(LocalDateTime.now());
        r.setHandlerMemo(req.handlerMemo());
    }

    public void updateCommentReportStatus(Long id, String adminUsername, ReportStatusUpdateRequest req) {
        CommentReport r = commentReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역 없음"));
        r.setStatus(req.status());
        r.setHandledBy(adminUsername);
        r.setHandledAt(LocalDateTime.now());
        r.setHandlerMemo(req.handlerMemo());
    }

    // DTO 변환
    private ReportDto toDto(PostReport r) {
        return new ReportDto(
                r.getId(), "POST", r.getPost().getId(),
                r.getPost().getTitle(),
                r.getPost().getContent(),  // ← 게시글 본문까지
                r.getReporter().getId(), r.getReporter().getNickname(),
                r.getReported().getId(), r.getReported().getNickname(),
                r.getReasons(), r.getDescription(), r.getStatus(),
                r.getCreatedAt(), r.getHandledBy(), r.getHandledAt(), r.getHandlerMemo()
        );
    }

    private ReportDto toDto(CommentReport r) {
        return new ReportDto(
                r.getId(), "COMMENT", r.getComment().getId(),
                null,                        // 댓글은 제목 없음
                r.getComment().getContent(), // 전체 본문
                r.getReporter().getId(), r.getReporter().getNickname(),
                r.getReported().getId(), r.getReported().getNickname(),
                r.getReasons(), r.getDescription(), r.getStatus(),
                r.getCreatedAt(), r.getHandledBy(), r.getHandledAt(), r.getHandlerMemo()
        );
    }
}