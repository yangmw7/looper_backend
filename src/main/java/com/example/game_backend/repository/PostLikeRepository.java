package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.Post;
import com.example.game_backend.repository.entity.PostLike;
import com.example.game_backend.repository.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByPostAndMember(Post post, Member member);
    void deleteByPostAndMember(Post post, Member member);
}