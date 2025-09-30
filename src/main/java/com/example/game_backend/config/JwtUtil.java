// src/main/java/com/example/game_backend/config/JwtUtil.java
package com.example.game_backend.config;
import com.example.game_backend.repository.entity.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {
    private final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private final long ACCESS_TOKEN_EXP = 1000 * 60 * 30;       // 30분
    private final long REFRESH_TOKEN_EXP = 1000 * 60 * 60 * 24 * 7; // 7일

    // Access Token 발급
    public String generateAccessToken(Member member) {
        return Jwts.builder()
                .setSubject(member.getUsername())
                .claim("nickname", member.getNickname())
                .claim("roles", List.of(member.getRole().name()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXP))
                .signWith(SECRET_KEY)
                .compact();
    }

    // Refresh Token 발급
    public String generateRefreshToken(Member member) {
        return Jwts.builder()
                .setSubject(member.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXP))
                .signWith(SECRET_KEY)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
