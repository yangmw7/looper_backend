package com.example.game_backend.controller.dto;

import lombok.Data;

@Data
public class JoinRequest {

    private String username;

    private String password;

    private String email;
}
