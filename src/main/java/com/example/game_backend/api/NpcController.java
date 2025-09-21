package com.example.game_backend.api;

import com.example.game_backend.controller.dto.NpcRequest;
import com.example.game_backend.controller.dto.NpcResponse;
import com.example.game_backend.service.NpcService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/npcs")
@RequiredArgsConstructor
public class NpcController {
    private final NpcService npcService;

    @PostMapping
    public NpcResponse createNpc(@RequestBody NpcRequest request) {
        return npcService.createNpc(request);
    }

    @GetMapping("/{id}")
    public NpcResponse getNpc(@PathVariable String id) {
        return npcService.getNpc(id);
    }

    @GetMapping
    public List<NpcResponse> getAllNpcs() {
        return npcService.getAllNpcs();
    }

    @PutMapping("/{id}")
    public NpcResponse updateNpc(@PathVariable String id, @RequestBody NpcRequest request) {
        return npcService.updateNpc(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteNpc(@PathVariable String id) {
        npcService.deleteNpc(id);
    }
}
