package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.Npc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NpcRepository extends JpaRepository<Npc, String> {
}
