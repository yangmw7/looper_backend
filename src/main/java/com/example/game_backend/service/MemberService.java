package com.example.game_backend.service;

import com.example.game_backend.controller.dto.JoinRequest;
import com.example.game_backend.controller.dto.LoginRequest;

public interface MemberService {

    String join(JoinRequest joinRequest);
    boolean login(LoginRequest loginRequest);
}

