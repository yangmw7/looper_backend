package com.example.game_backend.repository.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "skills")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    @Id
    private String id;

    @Column(name = "name", columnDefinition = "longtext")
    private String nameJson;

    @Column(name = "description", columnDefinition = "longtext")
    private String descriptionJson;

    @Column(name = "image_url")
    private String imageUrl;

    @Transient
    private List<String> name;

    @Transient
    private List<String> description;

    // ===== Setter =====
    public void setName(List<String> name) {
        this.name = name;
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.nameJson = (name != null) ? mapper.writeValueAsString(name) : null;
        } catch (Exception e) {
            this.nameJson = null;
        }
    }

    public void setDescription(List<String> description) {
        this.description = description;
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.descriptionJson = (description != null) ? mapper.writeValueAsString(description) : null;
        } catch (Exception e) {
            this.descriptionJson = null;
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // ===== JSON 변환 =====
    @PostLoad
    private void parseJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            if (nameJson != null) {
                this.name = mapper.readValue(nameJson, new TypeReference<List<String>>() {});
            }
            if (descriptionJson != null) {
                try {
                    this.description = mapper.readValue(descriptionJson, new TypeReference<List<String>>() {});
                } catch (Exception e) {
                    this.description = List.of(mapper.readValue(descriptionJson, String.class));
                }
            }
        } catch (Exception e) {
            this.name = List.of();
            this.description = List.of();
        }
    }

    @PrePersist
    @PreUpdate
    private void ensureJsonSync() {
        if (name != null && nameJson == null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.nameJson = mapper.writeValueAsString(name);
            } catch (Exception e) {
                this.nameJson = null;
            }
        }
        if (description != null && descriptionJson == null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.descriptionJson = mapper.writeValueAsString(description);
            } catch (Exception e) {
                this.descriptionJson = null;
            }
        }
    }
}
