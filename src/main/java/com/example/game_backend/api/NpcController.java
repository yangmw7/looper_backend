package com.example.game_backend.api;

import com.example.game_backend.controller.dto.NpcRequest;
import com.example.game_backend.controller.dto.NpcResponse;
import com.example.game_backend.service.NpcService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/npcs")
@RequiredArgsConstructor
public class NpcController {
    private final NpcService npcService;

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
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createNpc(@RequestBody NpcRequest request) {
        try {
            NpcResponse created = npcService.createNpc(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateNpc(@PathVariable String id, @RequestBody NpcRequest request) {
        NpcResponse updated = npcService.updateNpc(id, request);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteNpc(@PathVariable String id) {
        npcService.deleteNpc(id);
        return ResponseEntity.noContent().build();
    }
}
