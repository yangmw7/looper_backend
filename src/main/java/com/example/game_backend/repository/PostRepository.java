package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 추가: 특정 회원이 작성한 게시글 조회
    List<Post> findAllByWriterOrderByCreatedAtDesc(Member writer);
}