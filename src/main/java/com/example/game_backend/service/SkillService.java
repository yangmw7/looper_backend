package com.example.game_backend.service;

import com.example.game_backend.controller.dto.SkillRequest;
import com.example.game_backend.controller.dto.SkillResponse;

import java.util.List;

public interface SkillService {
    List<SkillResponse> getAllSkills();
    SkillResponse getSkill(String id);
    SkillResponse createSkill(SkillRequest request);
    SkillResponse updateSkill(String id, SkillRequest request);
    void deleteSkill(String id);
}
