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
// @Builder 제거 또는 아래처럼 수정
public class Skill {

    @Id
    private String id;

    @Column(name = "name", columnDefinition = "longtext")
    private String nameJson;

    @Column(name = "description", columnDefinition = "longtext")
    private String descriptionJson;

    @Transient
    private List<String> name;

    @Transient
    private List<String> description;

    // Setter를 명시적으로 호출하도록 수정
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

    // setId는 일반 Setter
    public void setId(String id) {
        this.id = id;
    }

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

    // @PrePersist/@PreUpdate는 백업용으로만
    @PrePersist
    @PreUpdate
    private void ensureJsonSync() {
        // Setter에서 이미 변환했지만, 혹시 모를 경우를 대비
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
