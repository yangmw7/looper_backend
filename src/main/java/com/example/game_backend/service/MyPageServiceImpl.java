package com.example.game_backend.service;

import com.example.game_backend.controller.dto.PasswordChangeRequestDto;
import com.example.game_backend.controller.dto.ProfileUpdateRequestDto;
import com.example.game_backend.controller.dto.MyPageResponseDto;
import com.example.game_backend.controller.dto.ProfileResponseDto;
import com.example.game_backend.controller.dto.StatsResponseDto;
import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.PlayerStatsRepository;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.entity.PlayerStats;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageServiceImpl implements MyPageService {

    private final MemberRepository memberRepository;
    private final PlayerStatsRepository playerStatsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MyPageResponseDto getMyPage(String username) {
        // Member 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        // PlayerStats 조회
        PlayerStats stats = playerStatsRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new IllegalArgumentException("게임 스탯을 찾을 수 없습니다"));

        return MyPageResponseDto.builder()
                .profile(ProfileResponseDto.from(member))
                .stats(StatsResponseDto.from(stats))
                .build();
    }

    @Override
    public ProfileResponseDto getProfile(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        return ProfileResponseDto.from(member);
    }

    @Override
    public StatsResponseDto getStats(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        PlayerStats stats = playerStatsRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new IllegalArgumentException("게임 스탯을 찾을 수 없습니다"));

        return StatsResponseDto.from(stats);
    }

    @Override
    @Transactional
    public ProfileResponseDto updateProfile(String username, ProfileUpdateRequestDto requestDto) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        // 닉네임 중복 체크 (자기 자신 제외)
        if (!member.getNickname().equals(requestDto.getNickname()) &&
                memberRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다");
        }

        // 이메일 중복 체크 (자기 자신 제외)
        if (!member.getEmail().equals(requestDto.getEmail()) &&
                memberRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
        }

        // 업데이트
        member.setNickname(requestDto.getNickname());
        member.setEmail(requestDto.getEmail());

        Member updated = memberRepository.save(member);
        return ProfileResponseDto.from(updated);
    }

    @Override
    @Transactional
    public void changePassword(String username, PasswordChangeRequestDto requestDto) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다");
        }

        // 새 비밀번호 확인
        if (!requestDto.getNewPassword().equals(requestDto.getNewPasswordConfirm())) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다");
        }

        // 비밀번호 변경
        member.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        memberRepository.save(member);
    }
}