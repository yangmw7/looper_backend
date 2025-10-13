package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.Appeal;
import com.example.game_backend.repository.entity.AppealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppealRepository extends JpaRepository<Appeal, Long> {

    Optional<Appeal> findByPenaltyId(Long penaltyId);

    List<Appeal> findByStatusOrderByCreatedAtDesc(AppealStatus status);

    @Query("SELECT a FROM Appeal a JOIN FETCH a.penalty p JOIN FETCH p.member ORDER BY a.createdAt DESC")
    List<Appeal> findAllWithPenaltyAndMember();
}