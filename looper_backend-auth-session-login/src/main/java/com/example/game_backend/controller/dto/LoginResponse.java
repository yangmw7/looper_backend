// dto/LoginResponse.java
package com.example.game_backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String nickname;
}
