package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, String> {
}
