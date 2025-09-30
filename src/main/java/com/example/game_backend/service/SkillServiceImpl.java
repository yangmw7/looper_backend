package com.example.game_backend.service;

import com.example.game_backend.controller.dto.SkillRequest;
import com.example.game_backend.controller.dto.SkillResponse;
import com.example.game_backend.repository.SkillRepository;
import com.example.game_backend.repository.entity.Skill;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    // ===== Entity → Response 변환 =====
    private SkillResponse toResponse(Skill skill) {
        return SkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName() != null ? skill.getName() : List.of("", ""))
                .description(skill.getDescription() != null ? skill.getDescription() : List.of("", ""))
                .build();
    }

    // ===== Request → Entity 변환 =====
    private Skill toEntity(SkillRequest request) {
        Skill skill = new Skill();
        skill.setId(request.getId());
        skill.setName(request.getName());               // setter가 nameJson 자동 동기화
        skill.setDescription(request.getDescription()); // setter가 descriptionJson 자동 동기화
        return skill;
    }

    // ===== CREATE =====
    @Override
    @Transactional
    public SkillResponse createSkill(SkillRequest request) {
        try {
            if (skillRepository.existsById(request.getId())) {
                throw new IllegalArgumentException("이미 존재하는 스킬 ID입니다: " + request.getId());
            }
            Skill saved = skillRepository.save(toEntity(request));
            return toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("이미 존재하는 스킬 ID입니다: " + request.getId());
        }
    }

    // ===== READ (단건) =====
    @Override
    public SkillResponse getSkill(String id) {
        return skillRepository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    // ===== READ (전체) =====
    @Override
    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // ===== UPDATE =====
    @Override
    @Transactional
    public SkillResponse updateSkill(String id, SkillRequest request) {
        return skillRepository.findById(id).map(skill -> {
            skill.setName(request.getName());               // setter → JSON 반영
            skill.setDescription(request.getDescription()); // setter → JSON 반영
            return toResponse(skillRepository.save(skill));
        }).orElse(null);
    }

    // ===== DELETE =====
    @Override
    public void deleteSkill(String id) {
        skillRepository.deleteById(id);
    }
}
