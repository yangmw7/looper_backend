package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
