package com.example.game_backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor // ← 이거 추가!
public class LoginRequest {
    private String username;
    private String password;
}
