package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PenaltyRepository extends JpaRepository<Penalty, Long> {

    // 특정 회원의 모든 제재 내역
    List<Penalty> findByMember_UsernameOrderByCreatedAtDesc(String username);

    // 특정 회원의 활성 제재만
    List<Penalty> findByMember_UsernameAndIsActiveTrueOrderByCreatedAtDesc(String username);
}