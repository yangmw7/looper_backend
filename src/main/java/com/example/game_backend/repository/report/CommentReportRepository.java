package com.example.game_backend.repository.report;

import com.example.game_backend.repository.entity.report.CommentReport;
import com.example.game_backend.repository.entity.report.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Set;

public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {
    boolean existsByComment_IdAndReporter_IdAndCreatedAtAfter(Long commentId, Long reporterId, LocalDateTime after);
    Page<CommentReport> findByStatusIn(Set<ReportStatus> statuses, Pageable pageable);
}