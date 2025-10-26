package com.example.game_backend.service;

import com.example.game_backend.controller.dto.AuthResponse;
import com.example.game_backend.controller.dto.LoginRequest;
import com.example.game_backend.controller.dto.TokenRefreshRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(TokenRefreshRequest request);
    void logout(String username);
    void deleteAccount(String username, String password);
}