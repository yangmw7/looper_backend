package com.example.game_backend.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "playerstats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate // 변경된 필드만 update (Dirty Checking 강화)
public class PlayerStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column @Builder.Default private Float hp = 0.0f;
    @Column @Builder.Default private Float atk = 0.0f;
    @Column @Builder.Default private Float def = 0.0f;
    @Column @Builder.Default private Float cri = 0.0f;
    @Column @Builder.Default private Float crid = 0.0f;
    @Column @Builder.Default private Float spd = 0.0f;
    @Column @Builder.Default private Float jmp = 0.0f;
    @Column @Builder.Default private Float ats = 0.0f;
    @Column @Builder.Default private Integer jcnt = 0;

    @Column(columnDefinition = "LONGTEXT")
    private String skills;

    @Column @Builder.Default private Integer clear = 0;
    @Column @Builder.Default private Integer chapter = 1;
    @Column @Builder.Default private Integer stage = 1;
    @Column private String mapid;

    @Column(columnDefinition = "LONGTEXT")
    private String equiped;

    @Column(columnDefinition = "LONGTEXT")
    private String inventory;
}
