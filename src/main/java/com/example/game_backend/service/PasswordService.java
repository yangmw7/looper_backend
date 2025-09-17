package com.example.game_backend.service;

import com.example.game_backend.controller.dto.ResetPasswordRequest;

public interface PasswordService {
    boolean verifyUser(ResetPasswordRequest request);
    void resetPassword(String username, String newPassword);
}
