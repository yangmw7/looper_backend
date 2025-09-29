package com.example.game_backend.service;

import com.example.game_backend.controller.dto.NpcRequest;
import com.example.game_backend.controller.dto.NpcResponse;

import java.util.List;

public interface NpcService {
    List<NpcResponse> getAllNpcs();
    NpcResponse getNpc(String id);
    NpcResponse createNpc(NpcRequest request);
    NpcResponse updateNpc(String id, NpcRequest request);
    void deleteNpc(String id);
}
