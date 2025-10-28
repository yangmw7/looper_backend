package com.example.game_backend.service;

import com.example.game_backend.controller.dto.SkillRequest;
import com.example.game_backend.controller.dto.SkillResponse;
import com.example.game_backend.repository.SkillRepository;
import com.example.game_backend.repository.entity.Skill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final CloudinaryService cloudinaryService;

    // List<String>에서 한글만 추출해서 List<String>으로 반환
    private List<String> extractKoreanAsList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }

        // 배열의 두 번째 요소(index 1)가 한글
        if (list.size() > 1) {
            return List.of(list.get(1)); // 한글만 List로 반환
        }

        // 한글이 포함된 문자열만 필터링
        return list.stream()
                .filter(s -> s != null && s.matches(".*[가-힣].*"))
                .toList();
    }

    @Override
    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SkillResponse getSkill(String id) {
        return skillRepository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    // ⭐ Admin용 - 전체 데이터 반환
    @Override
    public SkillResponse getSkillFull(String id) {
        return skillRepository.findById(id)
                .map(this::toResponseFull)
                .orElse(null);
    }

    @Override
    @Transactional
    public SkillResponse createSkill(SkillRequest request, MultipartFile imageFile) {
        // ID 중복 검사
        if (request.getId() == null || request.getId().isBlank()) {
            throw new IllegalArgumentException("Skill ID는 반드시 입력해야 합니다.");
        }
        if (skillRepository.existsById(request.getId())) {
            throw new DuplicateKeyException("이미 존재하는 Skill ID입니다: " + request.getId());
        }

        // Cloudinary에 이미지 업로드
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(imageFile, "skill_" + request.getId());
            request.setImageUrl(imageUrl);
            log.info("Skill {} 이미지 업로드 완료: {}", request.getId(), imageUrl);
        }

        Skill skill = new Skill();
        skill.setId(request.getId());
        skill.setName(request.getName());
        skill.setDescription(request.getDescription());
        skill.setImageUrl(request.getImageUrl());

        skillRepository.save(skill);
        return toResponseFull(skill); // ⭐ Admin용이므로 Full 반환
    }

    @Override
    @Transactional
    public SkillResponse updateSkill(String id, SkillRequest request, MultipartFile imageFile) {
        Optional<Skill> optionalSkill = skillRepository.findById(id);
        if (optionalSkill.isEmpty()) return null;

        Skill skill = optionalSkill.get();

        // 기존 이미지 URL 저장 (삭제용)
        String oldImageUrl = skill.getImageUrl();

        skill.setName(request.getName());
        skill.setDescription(request.getDescription());

        // 새 이미지 업로드
        if (imageFile != null && !imageFile.isEmpty()) {
            // 기존 이미지 삭제
            if (oldImageUrl != null) {
                cloudinaryService.deleteImage(oldImageUrl);
            }

            // 새 이미지 업로드
            String newImageUrl = cloudinaryService.uploadImage(imageFile, "skill_" + id);
            skill.setImageUrl(newImageUrl);
            log.info("Skill {} 이미지 업데이트 완료: {}", id, newImageUrl);
        }

        skillRepository.save(skill);
        return toResponseFull(skill); // ⭐ Admin용이므로 Full 반환
    }

    @Override
    @Transactional
    public void deleteSkill(String id) {
        skillRepository.findById(id).ifPresent(skill -> {
            // Cloudinary 이미지 삭제
            if (skill.getImageUrl() != null) {
                cloudinaryService.deleteImage(skill.getImageUrl());
                log.info("Skill {} 이미지 삭제 완료", id);
            }
            skillRepository.deleteById(id);
        });
    }

    // ⭐ 한글만 반환 (GameGuide용)
    private SkillResponse toResponse(Skill skill) {
        return new SkillResponse(
                skill.getId(),
                extractKoreanAsList(skill.getName()),
                extractKoreanAsList(skill.getDescription()),
                skill.getImageUrl()
        );
    }

    // ⭐ 전체 데이터 반환 (Admin용)
    private SkillResponse toResponseFull(Skill skill) {
        return new SkillResponse(
                skill.getId(),
                skill.getName(), // 영문/한글 모두 포함
                skill.getDescription(), // 영문/한글 모두 포함
                skill.getImageUrl()
        );
    }
}