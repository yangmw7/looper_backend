package com.example.game_backend.service;

import com.example.game_backend.controller.dto.JoinRequest;
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
        // 1) 이메일 중복 체크
        boolean emailExists = memberRepository.findByEmail(joinRequest.getEmail()).isPresent();
        if (emailExists) {
            return "fail_email";
        }

        // 2) 아이디 중복 체크
        boolean usernameExists = memberRepository.findByUsername(joinRequest.getUsername()).isPresent();
        if (usernameExists) {
            return "fail_username";
        }

        // 3) 비밀번호 암호화 후 저장
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
