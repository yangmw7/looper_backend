package com.example.game_backend.repository.report;

import com.example.game_backend.repository.entity.report.PostReport;
import com.example.game_backend.repository.entity.report.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {

    // 중복 신고 방지용
    boolean existsByPost_IdAndReporter_IdAndCreatedAtAfter(
            Long postId, Long reporterId, LocalDateTime after);

    // 관리자용: 상태별 조회
    Page<PostReport> findByStatusIn(Set<ReportStatus> statuses, Pageable pageable);

    // 마이페이지용: 내가 신고한 내역
    List<PostReport> findByReporter_UsernameOrderByCreatedAtDesc(String username);
}