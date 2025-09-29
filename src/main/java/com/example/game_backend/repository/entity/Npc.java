package com.example.game_backend.repository.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "npc")
@Getter
@Setter
public class Npc {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    private String id;

    @Lob
    @Column(name = "name")
    private String nameJson;

    private float hp;
    private float atk;
    private float def;
    private float spd;

    @Lob
    @Column(name = "features")
    private String featuresJson;

    // name getter/setter
    @Transient
    public List<String> getName() {
        if (nameJson == null || nameJson.isEmpty()) return new ArrayList<>();
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

    // features getter/setter
    @Transient
    public List<String> getFeatures() {
        if (featuresJson == null || featuresJson.isEmpty()) return new ArrayList<>();
        try {
            return objectMapper.readValue(featuresJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setFeatures(List<String> features) {
        if (features == null) {
            this.featuresJson = null;
            return;
        }
        try {
            this.featuresJson = objectMapper.writeValueAsString(features);
        } catch (JsonProcessingException e) {
            this.featuresJson = null;
        }
    }
}

