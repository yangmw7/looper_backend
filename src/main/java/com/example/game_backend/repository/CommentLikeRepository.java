// CommentLikeRepository.java

package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.Comment;
import com.example.game_backend.repository.entity.CommentLike;
import com.example.game_backend.repository.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    boolean existsByCommentAndMember(Comment comment, Member member);

    void deleteByCommentAndMember(Comment comment, Member member);

    // ⭐ 신규 추가: 사용자가 좋아요한 댓글 ID 목록 조회
    @Query("SELECT cl.comment.id FROM CommentLike cl " +
            "WHERE cl.comment.post.id = :postId AND cl.member.id = :memberId")
    List<Long> findLikedCommentIdsByPostAndMember(
            @Param("postId") Long postId,
            @Param("memberId") Long memberId);
}