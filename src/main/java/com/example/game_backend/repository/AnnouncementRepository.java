package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.Announcement;
import com.example.game_backend.repository.entity.AnnouncementCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // ========== 간단한 방식: 쿼리 최소화 ==========

    // 카테고리별 조회 (핀 포함, 정렬: 핀 우선 → 최신순)
    @Query("SELECT a FROM Announcement a WHERE a.category = :category ORDER BY a.isPinned DESC, a.createdAt DESC")
    Page<Announcement> findByCategoryOrderByPinned(AnnouncementCategory category, Pageable pageable);

    // 전체 조회 (핀 포함, 정렬: 핀 우선 → 최신순)
    @Query("SELECT a FROM Announcement a ORDER BY a.isPinned DESC, a.createdAt DESC")
    Page<Announcement> findAllOrderByPinned(Pageable pageable);

    // 현재 핀된 공지 개수 조회
    long countByIsPinnedTrue();

    // 가장 오래된 핀 조회 (FIFO용)
    Announcement findFirstByIsPinnedTrueOrderByPinnedAtAsc();
}