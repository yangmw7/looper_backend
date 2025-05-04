package com.example.game_backend.service;

import com.example.game_backend.controller.dto.JoinRequest;

public interface MemberService {

    String join(JoinRequest joinRequest);
}
