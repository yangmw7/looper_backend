package com.example.game_backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String nickname;
    private List<String> roles;  // ← 여기에 roles 추가
}
