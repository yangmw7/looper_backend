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

    @Override
    public NpcResponse getNpcFull(String id) {
        return npcRepository.findById(id)
                .map(this::toResponseFull)
                .orElse(null);
    }

    @Override
    @Transactional
    public NpcResponse createNpc(NpcRequest request, MultipartFile imageFile) {
        if (request.getId() == null || request.getId().isBlank()) {
            throw new IllegalArgumentException("NPC ID는 반드시 입력해야 합니다.");
        }
        if (npcRepository.existsById(request.getId())) {
            throw new DuplicateKeyException("이미 존재하는 NPC ID입니다: " + request.getId());
        }

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
        return toResponseFull(npc);
    }

    @Override
    @Transactional
    public NpcResponse updateNpc(String id, NpcRequest request, MultipartFile imageFile) {
        Optional<Npc> optionalNpc = npcRepository.findById(id);
        if (optionalNpc.isEmpty()) return null;

        Npc npc = optionalNpc.get();
        String oldImageUrl = npc.getImageUrl();

        npc.setName(request.getName());
        npc.setHp(request.getHp());
        npc.setAtk(request.getAtk());
        npc.setDef(request.getDef());
        npc.setSpd(request.getSpd());
        npc.setFeatures(request.getFeatures());

        if (imageFile != null && !imageFile.isEmpty()) {
            if (oldImageUrl != null) {
                cloudinaryService.deleteImage(oldImageUrl);
            }
            String newImageUrl = cloudinaryService.uploadImage(imageFile, "npc_" + id);
            npc.setImageUrl(newImageUrl);
            log.info("NPC {} 이미지 업데이트 완료: {}", id, newImageUrl);
        }

        npcRepository.save(npc);
        return toResponseFull(npc);
    }

    @Override
    @Transactional
    public void deleteNpc(String id) {
        npcRepository.findById(id).ifPresent(npc -> {
            if (npc.getImageUrl() != null) {
                cloudinaryService.deleteImage(npc.getImageUrl());
                log.info("NPC {} 이미지 삭제 완료", id);
            }
            npcRepository.deleteById(id);
        });
    }

    // 일반 사용자용 (GameGuide용)
    private NpcResponse toResponse(Npc npc) {
        return new NpcResponse(
                npc.getId(),
                npc.getName(),
                npc.getHp(),
                npc.getAtk(),
                npc.getDef(),
                npc.getSpd(),
                npc.getFeatures(),
                npc.getImageUrl()
        );
    }

    // 관리자용 (전체 데이터)
    private NpcResponse toResponseFull(Npc npc) {
        return new NpcResponse(
                npc.getId(),
                npc.getName(),
                npc.getHp(),
                npc.getAtk(),
                npc.getDef(),
                npc.getSpd(),
                npc.getFeatures(),
                npc.getImageUrl()
        );
    }
}
