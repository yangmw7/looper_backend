// src/main/java/com/example/game_backend/repository/CommentRepository.java
package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.Comment;
import com.example.game_backend.repository.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 게시물(Post)에 달린 댓글을 생성일(createdAt) 내림차순으로 가져오는 메서드
    List<Comment> findAllByPostOrderByCreatedAtDesc(Post post);
}
