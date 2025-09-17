package com.example.game_backend.service;

import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.entity.Post;
import com.example.game_backend.repository.entity.Comment;

import java.util.List;

public interface AdminService {
    // 회원 조회
    List<Member> getAllUsers();
    // 회원 삭제
    void deleteUser(Long userId);

    // 게시글 조회
    List<Post> getAllPosts();
    // 게시글 삭제
    void deletePost(Long postId);

    // 댓글 조회
    List<Comment> getAllComments();
    // 댓글 삭제
    void deleteComment(Long commentId);



}
