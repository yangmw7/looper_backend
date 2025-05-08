
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

    @Column(nullable = false, unique = true)
    private String username;  // 아이디

    @Column(nullable = false)
    private String password;  // 비밀번호 (암호화 예정)

    @Column(nullable = false)
    private String email;     // 이메일
}
