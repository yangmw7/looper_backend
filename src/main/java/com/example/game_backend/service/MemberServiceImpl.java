package com.example.game_backend.service;

import com.example.game_backend.controller.dto.JoinRequest;
import com.example.game_backend.controller.dto.LoginRequest;
import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder; // ← 주입 추가

    @Override
    public String join(JoinRequest joinRequest) {
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(joinRequest.getPassword());

        Member member = Member.builder()
                .username(joinRequest.getUsername())
                .password(encodedPassword) // 암호화된 비밀번호 저장
                .email(joinRequest.getEmail())
                .build();

        memberRepository.save(member);

        return "success";
    }

    @Override
    public boolean login(LoginRequest loginRequest) {
        Optional<Member> optionalMember = memberRepository.findByUsername(loginRequest.getUsername());

        if (optionalMember.isEmpty()) return false;

        Member member = optionalMember.get();
        return passwordEncoder.matches(loginRequest.getPassword(), member.getPassword());
    }
}



