package com.example.game_backend.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "item_names")
@Getter @Setter
public class ItemName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Item과 연관관계 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private String lang;   // ex) "en", "ko"
    private String value;  // ex) "Wooden Sword", "나무 검"
}
