package com.example.game_backend.service;

import com.example.game_backend.controller.dto.NpcRequest;
import com.example.game_backend.controller.dto.NpcResponse;
import com.example.game_backend.repository.NpcRepository;
import com.example.game_backend.repository.entity.Npc;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NpcServiceImpl implements NpcService {

    private final NpcRepository npcRepository;

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
    public NpcResponse createNpc(NpcRequest request) {
        Npc npc = new Npc();

        // 관리자가 직접 ID 입력 → 중복 검사
        if (request.getId() == null || request.getId().isBlank()) {
            throw new IllegalArgumentException("NPC ID는 반드시 입력해야 합니다.");
        }
        if (npcRepository.existsById(request.getId())) {
            throw new DuplicateKeyException("이미 존재하는 NPC ID입니다: " + request.getId());
        }
        npc.setId(request.getId());

        // 나머지 속성들 설정
        npc.setName(request.getName());
        npc.setHp(request.getHp());
        npc.setAtk(request.getAtk());
        npc.setDef(request.getDef());
        npc.setSpd(request.getSpd());
        npc.setFeatures(request.getFeatures());

        npcRepository.save(npc);
        return toResponse(npc);
    }

    @Override
    public NpcResponse updateNpc(String id, NpcRequest request) {
        Optional<Npc> optionalNpc = npcRepository.findById(id);
        if (optionalNpc.isEmpty()) return null;

        Npc npc = optionalNpc.get();
        npc.setName(request.getName());
        npc.setHp(request.getHp());
        npc.setAtk(request.getAtk());
        npc.setDef(request.getDef());
        npc.setSpd(request.getSpd());
        npc.setFeatures(request.getFeatures());

        npcRepository.save(npc);
        return toResponse(npc);
    }

    @Override
    public void deleteNpc(String id) {
        npcRepository.deleteById(id);
    }

    private NpcResponse toResponse(Npc npc) {
        return new NpcResponse(
                npc.getId(),
                npc.getName(),
                npc.getHp(),
                npc.getAtk(),
                npc.getDef(),
                npc.getSpd(),
                npc.getFeatures()
        );
    }
}
