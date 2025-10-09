package com.example.game_backend.repository.report;

import com.example.game_backend.repository.entity.report.PostReport;
import com.example.game_backend.repository.entity.report.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Set;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {
    boolean existsByPost_IdAndReporter_IdAndCreatedAtAfter(Long postId, Long reporterId, LocalDateTime after);
    Page<PostReport> findByStatusIn(Set<ReportStatus> statuses, Pageable pageable);
}