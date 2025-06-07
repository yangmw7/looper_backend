package com.example.game_backend.service;

import com.example.game_backend.controller.dto.PostRequest;

public interface PostService {
    Long save(PostRequest request, String writer);
    // 이미지 저장용 유틸 메서드를 public으로 열어두면, 컨트롤러에서 바로 호출해서 URL만 꺼낼 수도 있습니다.
    String storeFileAndGetUrl(org.springframework.web.multipart.MultipartFile file);



}
