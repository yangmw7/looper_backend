package com.example.game_backend.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "item_descriptions")
@Getter @Setter
public class ItemDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Item과 연관관계 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private String lang;   // ex) "en", "ko"
    private String value;  // ex) "An wooden sword.", "나무로 만든 검."
}
