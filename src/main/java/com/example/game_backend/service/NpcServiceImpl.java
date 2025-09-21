package com.example.game_backend.service;

import com.example.game_backend.controller.dto.NpcRequest;
import com.example.game_backend.controller.dto.NpcResponse;
import com.example.game_backend.repository.NpcRepository;
import com.example.game_backend.repository.entity.Npc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NpcServiceImpl implements NpcService {
    private final NpcRepository npcRepository;

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

    @Override
    public NpcResponse createNpc(NpcRequest request) {
        Npc npc = new Npc();
        npc.setId(request.getId());
        npc.setName(request.getName());
        npc.setHp(request.getHp());
        npc.setAtk(request.getAtk());
        npc.setDef(request.getDef());
        npc.setSpd(request.getSpd());
        npc.setFeatures(request.getFeatures());
        return toResponse(npcRepository.save(npc));
    }

    @Override
    public NpcResponse getNpc(String id) {
        return npcRepository.findById(id).map(this::toResponse).orElse(null);
    }

    @Override
    public List<NpcResponse> getAllNpcs() {
        return npcRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public NpcResponse updateNpc(String id, NpcRequest request) {
        return npcRepository.findById(id).map(npc -> {
            npc.setName(request.getName());
            npc.setHp(request.getHp());
            npc.setAtk(request.getAtk());
            npc.setDef(request.getDef());
            npc.setSpd(request.getSpd());
            npc.setFeatures(request.getFeatures());
            return toResponse(npcRepository.save(npc));
        }).orElse(null);
    }

    @Override
    public void deleteNpc(String id) {
        npcRepository.deleteById(id);
    }
}
