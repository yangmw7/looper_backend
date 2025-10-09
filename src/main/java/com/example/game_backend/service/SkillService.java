package com.example.game_backend.service;

import com.example.game_backend.controller.dto.SkillRequest;
import com.example.game_backend.controller.dto.SkillResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface SkillService {
    List<SkillResponse> getAllSkills();
    SkillResponse getSkill(String id);
    SkillResponse createSkill(SkillRequest request, MultipartFile imageFile);
    SkillResponse updateSkill(String id, SkillRequest request, MultipartFile imageFile);
    void deleteSkill(String id);
}
