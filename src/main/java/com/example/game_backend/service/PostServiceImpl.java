package com.example.game_backend.service;


import com.example.game_backend.controller.dto.PostRequest;
import com.example.game_backend.repository.PostRepository;
import com.example.game_backend.repository.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;

    @Override
    public Long save(PostRequest requestDto, String writer){
       Post post = Post.builder()
               .title(requestDto.getTitle())
               .content(requestDto.getContent())
               .writer(writer)
               .viewCount(0)
               .build();

        return postRepository.save(post).getId();

    }
}
