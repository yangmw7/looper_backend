package com.example.game_backend.service;

import com.example.game_backend.controller.dto.NpcRequest;
import com.example.game_backend.controller.dto.NpcResponse;

import java.util.List;

public interface NpcService {
    NpcResponse createNpc(NpcRequest request);
    NpcResponse getNpc(String id);
    List<NpcResponse> getAllNpcs();
    NpcResponse updateNpc(String id, NpcRequest request);
    void deleteNpc(String id);
}
