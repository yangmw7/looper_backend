package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUsername(String username);

    Optional<RefreshToken> findByToken(String token);

    @Modifying              // ← delete/update 쿼리에 필요
    @Transactional          // ← 트랜잭션 보장
    void deleteByUsername(String username);
}
