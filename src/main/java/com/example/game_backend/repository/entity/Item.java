package com.example.game_backend.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "items")
@Getter @Setter
public class Item {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    private String id;

    @Lob
    @Column(name = "name")
    private String nameJson;

    private String rarity;

    @Lob
    @Column(name = "description")
    private String descriptionJson;

    @Lob
    private String attributes;

    @Lob
    @Column(name = "skills")
    private String skillsJson;

    // name 필드에 대한 getter/setter
    @Transient
    public List<String> getName() {
        if (nameJson == null || nameJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(nameJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setName(List<String> name) {
        if (name == null) {
            this.nameJson = null;
            return;
        }
        try {
            this.nameJson = objectMapper.writeValueAsString(name);
        } catch (JsonProcessingException e) {
            this.nameJson = null;
        }
    }

    // description 필드에 대한 getter/setter
    @Transient
    public List<String> getDescription() {
        if (descriptionJson == null || descriptionJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(descriptionJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setDescription(List<String> description) {
        if (description == null) {
            this.descriptionJson = null;
            return;
        }
        try {
            this.descriptionJson = objectMapper.writeValueAsString(description);
        } catch (JsonProcessingException e) {
            this.descriptionJson = null;
        }
    }

    // skills 필드에 대한 getter/setter
    @Transient
    public List<String> getSkills() {
        if (skillsJson == null || skillsJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(skillsJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setSkills(List<String> skills) {
        if (skills == null) {
            this.skillsJson = null;
            return;
        }
        try {
            this.skillsJson = objectMapper.writeValueAsString(skills);
        } catch (JsonProcessingException e) {
            this.skillsJson = null;
        }
    }
}