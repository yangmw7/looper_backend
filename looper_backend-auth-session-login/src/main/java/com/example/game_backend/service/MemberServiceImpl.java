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
    private final PasswordEncoder passwordEncoder;

    @Override
    public String join(JoinRequest joinRequest) {
        // --------------------------------------
        // 1) 이메일 중복 체크 (추가된 부분)
        // --------------------------------------
        boolean emailExists = memberRepository.findByEmail(joinRequest.getEmail()).isPresent();
        if (emailExists) {
            // 이미 같은 이메일이 존재하는 경우
            return "fail_email";
        }

        // --------------------------------------
        // 2) 기존 로직: 아이디(Username) 중복 체크
        // --------------------------------------
        boolean usernameExists = memberRepository.findByUsername(joinRequest.getUsername()).isPresent();
        if (usernameExists) {
            return "fail_username";
        }

        // --------------------------------------
        // 3) 둘 다 중복이 아니면 회원가입 처리
        // --------------------------------------
        String encodedPassword = passwordEncoder.encode(joinRequest.getPassword());

        Member member = Member.builder()
                .username(joinRequest.getUsername())
                .nickname(joinRequest.getNickname())
                .password(encodedPassword)
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

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Override
    public Optional<Member> findByUsernameAndEmail(String username, String email) {
        return memberRepository.findByUsernameAndEmail(username, email);
    }

    @Override
    public void updatePassword(String username, String newPassword) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        String encodedPassword = passwordEncoder.encode(newPassword);
        member.setPassword(encodedPassword);
        memberRepository.save(member);
    }

    @Override
    public String findUsernameByEmail(String email) {
        Optional<Member> member = memberRepository.findByEmail(email);
        return member.map(Member::getUsername).orElse(null);
    }
}
