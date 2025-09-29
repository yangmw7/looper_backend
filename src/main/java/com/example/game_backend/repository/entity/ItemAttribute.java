package com.example.game_backend.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "item_attributes")
@Getter @Setter
public class ItemAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    // 스탯 타입: hp, atk, def, cri 등
    @Column(nullable = false)
    private String stat;

    // 연산 타입: add, mul
    @Column(nullable = false)
    private String op;

    // 적용 값
    @Column(nullable = false)
    private float value;
}
