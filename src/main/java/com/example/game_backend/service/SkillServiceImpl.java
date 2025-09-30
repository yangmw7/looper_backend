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

    private SkillResponse toResponse(Skill skill) {
        return SkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName())
                .description(skill.getDescription())
                .build();
    }

    private Skill toEntity(SkillRequest request) {
        return Skill.builder()
                .id(request.getId())
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

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

    @Override
    public SkillResponse getSkill(String id) {
        return skillRepository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SkillResponse updateSkill(String id, SkillRequest request) {
        return skillRepository.findById(id).map(skill -> {
            skill.setName(request.getName());
            skill.setDescription(request.getDescription());
            return toResponse(skillRepository.save(skill));
        }).orElse(null);
    }

    @Override
    public void deleteSkill(String id) {
        skillRepository.deleteById(id);
    }
}
