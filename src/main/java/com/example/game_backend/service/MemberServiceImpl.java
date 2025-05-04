package com.example.game_backend.service;

import com.example.game_backend.controller.dto.JoinRequest;
import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Override
    public String join(JoinRequest joinRequest) {
        Member member = Member.builder()
                .username(joinRequest.getUsername())
                .password(joinRequest.getPassword())
                .email(joinRequest.getEmail())
                .build();
        memberRepository.save(member);

        return "success";
    }
}
