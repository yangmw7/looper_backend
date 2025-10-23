package com.example.game_backend.service;

import com.example.game_backend.controller.dto.EquipItemRequest;
import com.example.game_backend.controller.dto.PlayerEquipmentResponse;

public interface PlayerEquipmentService {

    PlayerEquipmentResponse getPlayerEquipment(String username);

    PlayerEquipmentResponse equipItem(String username, EquipItemRequest request);

    PlayerEquipmentResponse unequipItem(String username, String slot);
}