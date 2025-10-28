package com.example.game_backend.service;

import com.example.game_backend.controller.dto.NpcRequest;
import com.example.game_backend.controller.dto.NpcResponse;
import com.example.game_backend.repository.NpcRepository;
import com.example.game_backend.repository.entity.Npc;
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
public class NpcServiceImpl implements NpcService {

    private final NpcRepository npcRepository;
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
    public List<NpcResponse> getAllNpcs() {
        return npcRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public NpcResponse getNpc(String id) {
        return npcRepository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    // ⭐ Admin용 - 전체 데이터 반환
    @Override
    public NpcResponse getNpcFull(String id) {
        return npcRepository.findById(id)
                .map(this::toResponseFull)
                .orElse(null);
    }

    @Override
    @Transactional
    public NpcResponse createNpc(NpcRequest request, MultipartFile imageFile) {
        // ID 중복 검사
        if (request.getId() == null || request.getId().isBlank()) {
            throw new IllegalArgumentException("NPC ID는 반드시 입력해야 합니다.");
        }
        if (npcRepository.existsById(request.getId())) {
            throw new DuplicateKeyException("이미 존재하는 NPC ID입니다: " + request.getId());
        }

        // Cloudinary에 이미지 업로드
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(imageFile, "npc_" + request.getId());
            request.setImageUrl(imageUrl);
            log.info("NPC {} 이미지 업로드 완료: {}", request.getId(), imageUrl);
        }

        Npc npc = new Npc();
        npc.setId(request.getId());
        npc.setName(request.getName());
        npc.setHp(request.getHp());
        npc.setAtk(request.getAtk());
        npc.setDef(request.getDef());
        npc.setSpd(request.getSpd());
        npc.setFeatures(request.getFeatures());
        npc.setImageUrl(request.getImageUrl());

        npcRepository.save(npc);
        return toResponseFull(npc); // ⭐ Admin용이므로 Full 반환
    }

    @Override
    @Transactional
    public NpcResponse updateNpc(String id, NpcRequest request, MultipartFile imageFile) {
        Optional<Npc> optionalNpc = npcRepository.findById(id);
        if (optionalNpc.isEmpty()) return null;

        Npc npc = optionalNpc.get();

        // 기존 이미지 URL 저장 (삭제용)
        String oldImageUrl = npc.getImageUrl();

        npc.setName(request.getName());
        npc.setHp(request.getHp());
        npc.setAtk(request.getAtk());
        npc.setDef(request.getDef());
        npc.setSpd(request.getSpd());
        npc.setFeatures(request.getFeatures());

        // 새 이미지 업로드
        if (imageFile != null && !imageFile.isEmpty()) {
            // 기존 이미지 삭제
            if (oldImageUrl != null) {
                cloudinaryService.deleteImage(oldImageUrl);
            }

            // 새 이미지 업로드
            String newImageUrl = cloudinaryService.uploadImage(imageFile, "npc_" + id);
            npc.setImageUrl(newImageUrl);
            log.info("NPC {} 이미지 업데이트 완료: {}", id, newImageUrl);
        }

        npcRepository.save(npc);
        return toResponseFull(npc); // ⭐ Admin용이므로 Full 반환
    }

    @Override
    @Transactional
    public void deleteNpc(String id) {
        npcRepository.findById(id).ifPresent(npc -> {
            // Cloudinary 이미지 삭제
            if (npc.getImageUrl() != null) {
                cloudinaryService.deleteImage(npc.getImageUrl());
                log.info("NPC {} 이미지 삭제 완료", id);
            }
            npcRepository.deleteById(id);
        });
    }

    // ⭐ 한글만 반환 (GameGuide용)
    private NpcResponse toResponse(Npc npc) {
        return new NpcResponse(
                npc.getId(),
                extractKoreanAsList(npc.getName()),
                npc.getHp(),
                npc.getAtk(),
                npc.getDef(),
                npc.getSpd(),
                extractKoreanAsList(npc.getFeatures()),
                npc.getImageUrl()
        );
    }

    // ⭐ 전체 데이터 반환 (Admin용)
    private NpcResponse toResponseFull(Npc npc) {
        return new NpcResponse(
                npc.getId(),
                npc.getName(), // 영문/한글 모두 포함
                npc.getHp(),
                npc.getAtk(),
                npc.getDef(),
                npc.getSpd(),
                npc.getFeatures(), // 전체 features 포함
                npc.getImageUrl()
        );
    }
}