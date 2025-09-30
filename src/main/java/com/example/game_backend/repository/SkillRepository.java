package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, String> {
}
