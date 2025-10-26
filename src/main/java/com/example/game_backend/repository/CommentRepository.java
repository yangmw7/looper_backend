package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.Comment;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPostAndParentCommentIsNullOrderByCreatedAtDesc(Post post);
    List<Comment> findAllByPostOrderByCreatedAtDesc(Post post);

    // 추가: 특정 회원이 작성한 댓글 조회
    List<Comment> findAllByMemberOrderByCreatedAtDesc(Member member);
}