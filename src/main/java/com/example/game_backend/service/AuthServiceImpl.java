package com.example.game_backend.service;

import com.example.game_backend.config.JwtUtil;
import com.example.game_backend.controller.dto.AuthResponse;
import com.example.game_backend.controller.dto.LoginRequest;
import com.example.game_backend.controller.dto.TokenRefreshRequest;
import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.RefreshTokenRepository;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.entity.RefreshToken;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Member member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("비밀번호 불일치");
        }

        String accessToken = jwtUtil.generateAccessToken(member);
        String refreshToken = jwtUtil.generateRefreshToken(member);

        refreshTokenRepository.deleteByUsername(member.getUsername());
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .username(member.getUsername())
                        .token(refreshToken)
                        .expiryDate(LocalDateTime.now().plusDays(7))
                        .build()
        );

        return new AuthResponse(
                accessToken,
                refreshToken,
                member.getNickname(),
                List.of(member.getRole().name())
        );
    }

    @Override
    @Transactional
    public AuthResponse refresh(TokenRefreshRequest request) {
        String oldRefreshToken = request.getRefreshToken();
        Claims claims = jwtUtil.extractAllClaims(oldRefreshToken);
        String username = claims.getSubject();

        RefreshToken saved = refreshTokenRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (!saved.getToken().equals(oldRefreshToken) || jwtUtil.isTokenExpired(oldRefreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.generateAccessToken(member);
        String newRefreshToken = jwtUtil.generateRefreshToken(member);

        refreshTokenRepository.deleteByUsername(username);
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .username(username)
                        .token(newRefreshToken)
                        .expiryDate(LocalDateTime.now().plusDays(7))
                        .build()
        );

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                member.getNickname(),
                List.of(member.getRole().name())
        );
    }

    @Override
    @Transactional
    public void logout(String username) {
        refreshTokenRepository.deleteByUsername(username);
    }

    @Override
    @Transactional
    public void deleteAccount(String username, String password) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다"));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }

        refreshTokenRepository.deleteByUsername(username);

        memberRepository.delete(member);
    }
}