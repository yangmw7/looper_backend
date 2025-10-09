package com.example.game_backend.api;

import com.example.game_backend.controller.dto.NpcRequest;
import com.example.game_backend.controller.dto.NpcResponse;
import com.example.game_backend.service.NpcService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
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
@RequestMapping("/api/npcs")
@RequiredArgsConstructor
public class NpcController {
    private final NpcService npcService;
    private final ObjectMapper objectMapper;

    // ========== 모든 사용자 접근 가능 ==========
    @GetMapping
    public List<NpcResponse> getAllNpcs() {
        return npcService.getAllNpcs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NpcResponse> getNpc(@PathVariable String id) {
        NpcResponse npc = npcService.getNpc(id);
        if (npc == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(npc);
    }

    // ========== 관리자만 접근 가능 ==========
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createNpc(
            @RequestPart("npc") String npcJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        try {
            // JSON 문자열을 NpcRequest 객체로 변환
            NpcRequest request = objectMapper.readValue(npcJson, NpcRequest.class);

            log.info("NPC 생성 요청: ID={}, 이미지={}", request.getId(),
                    imageFile != null ? imageFile.getOriginalFilename() : "없음");

            NpcResponse created = npcService.createNpc(request, imageFile);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (DuplicateKeyException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            log.error("NPC 생성 실패", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "NPC 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateNpc(
            @PathVariable String id,
            @RequestPart("npc") String npcJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        try {
            // JSON 문자열을 NpcRequest 객체로 변환
            NpcRequest request = objectMapper.readValue(npcJson, NpcRequest.class);

            log.info("NPC 수정 요청: ID={}, 이미지={}", id,
                    imageFile != null ? imageFile.getOriginalFilename() : "없음");

            NpcResponse updated = npcService.updateNpc(id, request, imageFile);
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("NPC 수정 실패", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "NPC 수정 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteNpc(@PathVariable String id) {
        npcService.deleteNpc(id);
        return ResponseEntity.noContent().build();
    }
}