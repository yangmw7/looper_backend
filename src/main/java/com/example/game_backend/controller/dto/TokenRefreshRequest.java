package com.example.game_backend.controller.dto;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refreshToken;
}
