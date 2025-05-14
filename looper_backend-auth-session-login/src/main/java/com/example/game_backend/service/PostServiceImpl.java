// service/PostServiceImpl.java
package com.example.game_backend.service;

import com.example.game_backend.controller.dto.PostRequest;
import com.example.game_backend.repository.entity.Post;
import com.example.game_backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Override
    public Long save(PostRequest request, String writer) {
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .writer(writer)
                .viewCount(0) // 처음엔 조회수 0
                .build();
        return postRepository.save(post).getId();
    }
}
