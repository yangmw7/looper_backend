package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.PlayerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerStatsRepository extends JpaRepository<PlayerStats, Long> {

    // Member ID로 PlayerStats 조회
    Optional<PlayerStats> findByMemberId(Long memberId);

    // Member ID로 존재 여부 확인
    boolean existsByMemberId(Long memberId);

    // Member ID로 삭제
    void deleteByMemberId(Long memberId);
}