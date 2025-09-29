package com.example.game_backend.service;

import com.example.game_backend.controller.dto.NpcRequest;
import com.example.game_backend.controller.dto.NpcResponse;
import com.example.game_backend.repository.NpcRepository;
import com.example.game_backend.repository.entity.Npc;
import com.example.game_backend.service.NpcService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        npc.setId(UUID.randomUUID().toString());
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
