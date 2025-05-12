package com.example.game_backend.repository.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userid", nullable = false, unique = true)
    private String username;  // 아이디

    @Column(nullable = false)
    private String password;  // 비밀번호 (암호화 예정)

    @Column(nullable = false, unique = true)
    private String email;     // 이메일

    @Column(nullable = false, unique = true)
    private String nickname;  // 닉네임 ← 게시판에 표시용
}
