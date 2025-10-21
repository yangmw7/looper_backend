package com.example.game_backend.repository.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "playerstats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Member와 1:1 관계 (외래키)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    // 스탯 정보
    @Column
    @Builder.Default
    private Float hp = 0.0f;

    @Column
    @Builder.Default
    private Float atk = 0.0f;

    @Column
    @Builder.Default
    private Float def = 0.0f;

    @Column
    @Builder.Default
    private Float cri = 0.0f;  // 크리티컬 확률

    @Column
    @Builder.Default
    private Float crid = 0.0f;  // 크리티컬 데미지

    @Column
    @Builder.Default
    private Float spd = 0.0f;

    @Column
    @Builder.Default
    private Float jmp = 0.0f;

    @Column
    @Builder.Default
    private Float ats = 0.0f;

    @Column
    @Builder.Default
    private Integer jcnt = 0;

    @Column(columnDefinition = "LONGTEXT")
    private String skills;

    // 진행도 정보
    @Column
    @Builder.Default
    private Integer clear = 0;

    @Column
    @Builder.Default
    private Integer chapter = 1;

    @Column
    @Builder.Default
    private Integer stage = 1;

    @Column
    private String mapid;

    // JSON 형태로 저장될 장비/인벤토리
    @Column(columnDefinition = "LONGTEXT")
    private String equiped;

    @Column(columnDefinition = "LONGTEXT")
    private String inventory;
}