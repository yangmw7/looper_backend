package com.example.game_backend.service;

import com.example.game_backend.repository.CommentRepository;
import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.PostRepository;
import com.example.game_backend.repository.entity.Comment;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.entity.Post;
import com.example.game_backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final MemberRepository memberRepo;
    private final PostRepository postRepo;
    private final CommentRepository commentRepo;

    @Override
    public List<Member> getAllUsers() {
        return memberRepo.findAll();
    }

    @Override
    public void deleteUser(Long userId) {
        memberRepo.deleteById(userId);
    }

    @Override
    public List<Post> getAllPosts() {
        return postRepo.findAll();
    }

    @Override
    public void deletePost(Long postId) {
        postRepo.deleteById(postId);
    }

    @Override
    public List<Comment> getAllComments() {
        return commentRepo.findAll();
    }

    @Override
    public void deleteComment(Long commentId) {
        commentRepo.deleteById(commentId);
    }


}
