package com.example.game_backend.api;

import com.example.game_backend.controller.dto.SkillRequest;
import com.example.game_backend.controller.dto.SkillResponse;
import com.example.game_backend.service.SkillService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;
    private final ObjectMapper objectMapper;

    // ========== 모든 사용자 접근 가능 ==========
    @GetMapping
    public List<SkillResponse> getAllSkills() {
        return skillService.getAllSkills();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillResponse> getSkill(@PathVariable String id) {
        SkillResponse skill = skillService.getSkill(id);
        if (skill == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(skill);
    }

    // ========== 관리자 전용 ==========
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createSkill(
            @RequestPart("skill") String skillJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        try {
            SkillRequest request = objectMapper.readValue(skillJson, SkillRequest.class);
            log.info("스킬 생성 요청: ID={}, 이미지={}", request.getId(),
                    imageFile != null ? imageFile.getOriginalFilename() : "없음");

            SkillResponse created = skillService.createSkill(request, imageFile);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (Exception e) {
            log.error("스킬 생성 실패", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "스킬 생성 중 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSkill(
            @PathVariable String id,
            @RequestPart("skill") String skillJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        try {
            SkillRequest request = objectMapper.readValue(skillJson, SkillRequest.class);
            log.info("스킬 수정 요청: ID={}, 이미지={}", id,
                    imageFile != null ? imageFile.getOriginalFilename() : "없음");

            SkillResponse updated = skillService.updateSkill(id, request, imageFile);
            if (updated == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            log.error("스킬 수정 실패", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "스킬 수정 중 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSkill(@PathVariable String id) {
        skillService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }
}
