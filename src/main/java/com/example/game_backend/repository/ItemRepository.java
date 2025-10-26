package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, String> {

    /**
     * 여러 아이템 ID로 아이템 기본 정보 조회
     * 관계는 LAZY 로딩으로 처리
     */
    @Query("SELECT i FROM Item i WHERE i.id IN :itemIds")
    List<Item> findAllByIdWithDetails(@Param("itemIds") List<String> itemIds);

    /**
     * 단일 아이템 ID로 아이템 기본 정보 조회
     */
    @Query("SELECT i FROM Item i WHERE i.id = :itemId")
    Optional<Item> findByIdWithDetails(@Param("itemId") String itemId);
}