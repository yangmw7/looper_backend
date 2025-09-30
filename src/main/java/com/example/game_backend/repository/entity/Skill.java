package com.example.game_backend.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {

    @Id
    private String id;

    @ElementCollection
    @CollectionTable(name = "skill_names", joinColumns = @JoinColumn(name = "skill_id"))
    @Column(name = "name")
    private List<String> name;

    @ElementCollection
    @CollectionTable(name = "skill_descriptions", joinColumns = @JoinColumn(name = "skill_id"))
    @Column(name = "description")
    private List<String> description;
}
