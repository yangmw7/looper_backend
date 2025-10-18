package com.example.game_backend.controller.dto.announcement;

import com.example.game_backend.repository.entity.AnnouncementCategory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Getter
@Setter
public class AnnouncementRequest {
    private String title;
    private String content;
    private AnnouncementCategory category;
    private List<String> keepImageUrls;
    private MultipartFile[] imageFiles;
}