package com.example.game_backend.service;

import com.example.game_backend.controller.dto.NpcRequest;
import com.example.game_backend.controller.dto.NpcResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NpcService {
    List<NpcResponse> getAllNpcs();
    NpcResponse getNpc(String id);
    NpcResponse getNpcFull(String id); // ⭐ Admin용 추가
    NpcResponse createNpc(NpcRequest request, MultipartFile imageFile);
    NpcResponse updateNpc(String id, NpcRequest request, MultipartFile imageFile);
    void deleteNpc(String id);
}