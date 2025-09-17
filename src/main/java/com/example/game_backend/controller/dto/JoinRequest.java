package com.example.game_backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor            // 기본 생성자
@AllArgsConstructor           // 모든 필드를 받는 생성자
public class JoinRequest {
    private String username;
    private String password;
    private String email;
    private String nickname;
}
