// service/PostService.java
package com.example.game_backend.service;

import com.example.game_backend.controller.dto.PostRequest;

public interface PostService {
    Long save(PostRequest request, String writer);
}
