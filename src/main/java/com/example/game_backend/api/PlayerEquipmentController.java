package com.example.game_backend.api;

import com.example.game_backend.controller.dto.EquipItemRequest;
import com.example.game_backend.controller.dto.PlayerEquipmentResponse;
import com.example.game_backend.controller.dto.UnequipItemRequest;
import com.example.game_backend.service.PlayerEquipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/mypage/equipment")
@RequiredArgsConstructor
public class PlayerEquipmentController {

    private final PlayerEquipmentService equipmentService;

    /**
     * GET /api/mypage/equipment
     * 장비 + 인벤토리 + 스탯 전체 조회
     */
    @GetMapping
    public ResponseEntity<PlayerEquipmentResponse> getEquipment(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("========== 장비 정보 조회 API 호출됨 ==========");
        log.info("UserDetails: {}", userDetails);
        log.info("Username: {}", userDetails != null ? userDetails.getUsername() : "null");

        try {
            PlayerEquipmentResponse response = equipmentService
                    .getPlayerEquipment(userDetails.getUsername());

            log.info("응답 데이터 - 스탯: {}", response.getStats());
            log.info("응답 데이터 - 인벤토리 크기: {}", response.getInventory() != null ? response.getInventory().size() : "null");
            log.info("응답 데이터 - 장착 장비 크기: {}", response.getEquipped() != null ? response.getEquipped().size() : "null");
            log.info("========== 장비 정보 조회 성공 ==========");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("========== 장비 정보 조회 실패 ==========", e);
            throw e;
        }
    }

    /**
     * POST /api/mypage/equipment/equip
     * 장비 장착
     */
    @PostMapping("/equip")
    public ResponseEntity<PlayerEquipmentResponse> equipItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody EquipItemRequest request) {

        log.info("========== 장비 장착 API 호출됨 ==========");
        log.info("사용자: {}, 아이템: {}, 슬롯: {}",
                userDetails.getUsername(), request.getItemId(), request.getSlot());

        try {
            PlayerEquipmentResponse response = equipmentService
                    .equipItem(userDetails.getUsername(), request);

            log.info("========== 장비 장착 성공 ==========");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("========== 장비 장착 실패 ==========", e);
            throw e;
        }
    }

    /**
     * POST /api/mypage/equipment/unequip
     * 장비 해제
     */
    @PostMapping("/unequip")
    public ResponseEntity<PlayerEquipmentResponse> unequipItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UnequipItemRequest request) {

        log.info("========== 장비 해제 API 호출됨 ==========");
        log.info("사용자: {}, 슬롯: {}", userDetails.getUsername(), request.getSlot());

        try {
            PlayerEquipmentResponse response = equipmentService
                    .unequipItem(userDetails.getUsername(), request.getSlot());

            log.info("========== 장비 해제 성공 ==========");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("========== 장비 해제 실패 ==========", e);
            throw e;
        }
    }
}