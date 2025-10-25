package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.Comment;
import com.example.game_backend.repository.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 부모 댓글이 없는 최상위 댓글만 조회 (대댓글 제외)
    List<Comment> findAllByPostAndParentCommentIsNullOrderByCreatedAtDesc(Post post);

    List<Comment> findAllByPostOrderByCreatedAtDesc(Post post);
}