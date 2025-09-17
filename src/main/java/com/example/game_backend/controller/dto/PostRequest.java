package com.example.game_backend.controller.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class PostRequest {
    private String title;
    private String content;

    /** 기존에 그대로 남길 이미지 URL 리스트 */
    private List<String> keepImageUrls;

    /** 새로 업로드할 파일들 */
    private MultipartFile[] imageFiles;
}
