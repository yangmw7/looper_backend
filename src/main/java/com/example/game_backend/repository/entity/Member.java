// src/main/java/com/example/game_backend/repository/entity/Member.java
package com.example.game_backend.repository.entity;

import com.example.game_backend.security.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userid", nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    // ========== 🆕 신고/제재 관련 필드 추가 ==========

    /**
     * 총 신고당한 횟수
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer reportCount = 0;

    /**
     * 경고 누적 횟수
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer warningCount = 0;

    /**
     * 정지 누적 횟수
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer suspensionCount = 0;

    /**
     * 계정 활성화 여부 (영구정지 시 false)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    // ========== 기존 필드 ==========

    // PlayerStats와 1:1 양방향 관계
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PlayerStats playerStats;

    // 편의 메서드
    public void setPlayerStats(PlayerStats playerStats) {
        this.playerStats = playerStats;
        if (playerStats != null) {
            playerStats.setMember(this);
        }
    }
}