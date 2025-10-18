package com.example.game_backend.repository.report;

import com.example.game_backend.repository.entity.report.AnnouncementCommentReport;
import com.example.game_backend.repository.entity.report.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface AnnouncementCommentReportRepository extends JpaRepository<AnnouncementCommentReport, Long> {

    // 중복 신고 방지
    boolean existsByAnnouncementComment_IdAndReporter_IdAndCreatedAtAfter(
            Long commentId, Long reporterId, LocalDateTime time);

    // 상태별 조회
    Page<AnnouncementCommentReport> findByStatusIn(Set<ReportStatus> statuses, Pageable pageable);

    // 내가 신고한 내역 조회
    List<AnnouncementCommentReport> findByReporter_UsernameOrderByCreatedAtDesc(String username);
}