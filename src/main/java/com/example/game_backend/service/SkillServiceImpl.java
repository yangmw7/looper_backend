package com.example.game_backend.service;

import com.example.game_backend.controller.dto.SkillRequest;
import com.example.game_backend.controller.dto.SkillResponse;
import com.example.game_backend.repository.SkillRepository;
import com.example.game_backend.repository.entity.Skill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final CloudinaryService cloudinaryService;

    // ===== Entity → Response 변환 =====
    private SkillResponse toResponse(Skill skill) {
        return SkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName() != null ? skill.getName() : List.of("", ""))
                .description(skill.getDescription() != null ? skill.getDescription() : List.of("", ""))
                .imageUrl(skill.getImageUrl())
                .build();
    }

    // ===== Request → Entity 변환 =====
    private Skill toEntity(SkillRequest request) {
        Skill skill = new Skill();
        skill.setId(request.getId());
        skill.setName(request.getName());
        skill.setDescription(request.getDescription());
        skill.setImageUrl(request.getImageUrl());
        return skill;
    }

    // ===== CREATE =====
    @Override
    @Transactional
    public SkillResponse createSkill(SkillRequest request, MultipartFile imageFile) {
        try {
            if (skillRepository.existsById(request.getId())) {
                throw new IllegalArgumentException("이미 존재하는 스킬 ID입니다: " + request.getId());
            }

            Skill skill = toEntity(request);

            // Cloudinary 업로드 (폴더 자동 분기)
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(imageFile, "skill_" + request.getId());
                skill.setImageUrl(imageUrl);
            }

            Skill saved = skillRepository.save(skill);
            return toResponse(saved);

        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("이미 존재하는 스킬 ID입니다: " + request.getId());
        } catch (Exception e) {
            throw new RuntimeException("스킬 생성 실패: " + e.getMessage(), e);
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
    public SkillResponse updateSkill(String id, SkillRequest request, MultipartFile imageFile) {
        return skillRepository.findById(id).map(skill -> {
            skill.setName(request.getName());
            skill.setDescription(request.getDescription());

            try {
                if (imageFile != null && !imageFile.isEmpty()) {
                    // 기존 이미지 삭제
                    if (skill.getImageUrl() != null) {
                        cloudinaryService.deleteImage(skill.getImageUrl());
                    }

                    // 새 이미지 업로드 (폴더 자동 분기)
                    String newImageUrl = cloudinaryService.uploadImage(imageFile, "skill_" + id);
                    skill.setImageUrl(newImageUrl);
                }
            } catch (Exception e) {
                log.error("스킬 이미지 수정 실패: {}", e.getMessage());
            }

            Skill updated = skillRepository.save(skill);
            return toResponse(updated);
        }).orElse(null);
    }

    // ===== DELETE =====
    @Override
    public void deleteSkill(String id) {
        skillRepository.findById(id).ifPresent(skill -> {
            if (skill.getImageUrl() != null) {
                cloudinaryService.deleteImage(skill.getImageUrl());
            }
            skillRepository.delete(skill);
        });
    }
}
