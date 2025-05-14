package com.example.game_backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordChangeRequest {
    private String username;
    private String newPassword;
    private String confirmPassword;
}
