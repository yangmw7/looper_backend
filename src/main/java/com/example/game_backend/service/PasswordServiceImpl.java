package com.example.game_backend.service;

import com.example.game_backend.controller.dto.ResetPasswordRequest;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean verifyUser(ResetPasswordRequest request) {
        return memberRepository.findByUsernameAndEmail(request.getUsername(), request.getEmail()).isPresent();
    }

    @Override
    public void resetPassword(String username, String newPassword) {
        Optional<Member> optional = memberRepository.findByUsername(username);
        optional.ifPresent(member -> {
            member.setPassword(passwordEncoder.encode(newPassword));
            memberRepository.save(member);
        });
    }
}
