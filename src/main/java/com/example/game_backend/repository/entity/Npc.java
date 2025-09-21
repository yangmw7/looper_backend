package com.example.game_backend.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "npc")
@Getter @Setter
public class Npc {
    @Id
    private String id;

    private String name;
    private Float hp;
    private Float atk;
    private Float def;
    private Float spd;

    @ElementCollection
    private List<String> features;
}
