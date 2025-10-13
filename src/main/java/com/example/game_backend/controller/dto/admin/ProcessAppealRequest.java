package com.example.game_backend.controller.dto.admin;

public record ProcessAppealRequest(
        Boolean approve, // true: 승인, false: 거부
        String adminResponse
) {
}