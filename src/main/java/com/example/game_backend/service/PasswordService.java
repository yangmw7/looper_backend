package com.example.game_backend.service;

import com.example.game_backend.controller.dto.FindPasswordRequest;

public interface PasswordService {
    boolean verifyUser(FindPasswordRequest request);
    void resetPassword(String username, String newPassword);
}
