package com.example.game_backend.service;

import com.example.game_backend.controller.dto.JoinRequest;
import com.example.game_backend.repository.entity.Member;

import java.util.Optional;

public interface MemberService {
    String join(JoinRequest joinRequest);
    Optional<Member> findByEmail(String email);
    String findUsernameByEmail(String email);
    Optional<Member> findByUsernameAndEmail(String userid, String email);
    void updatePassword(String username, String newPassword);
}
