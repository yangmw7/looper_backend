package com.example.game_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String publicId) {
        try {
            String folder;
            if (publicId.startsWith("post_")) {
                folder = "looper-posts";
            } else if (publicId.startsWith("npc_")) {
                folder = "looper-npcs";
            } else if (publicId.startsWith("skill_")) {
                folder = "looper-skills";
            } else if (publicId.startsWith("announcement_")) {
                folder = "looper-announcements";
            } else {
                folder = "looper-items";
            }

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "public_id", publicId,
                            "overwrite", true,
                            "resource_type", "auto"
                    )
            );

            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("이미지 업로드 성공: folder={}, url={}", folder, imageUrl);
            return imageUrl;

        } catch (IOException e) {
            log.error("이미지 업로드 실패", e);
            throw new RuntimeException("이미지 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return;
        }

        try {
            String fullPublicId = extractFullPublicId(imageUrl);
            cloudinary.uploader().destroy(fullPublicId, ObjectUtils.emptyMap());
            log.info("이미지 삭제 성공: {}", fullPublicId);
        } catch (IOException e) {
            log.error("이미지 삭제 실패: {}", e.getMessage());
        }
    }

    private String extractFullPublicId(String url) {
        try {
            String[] parts = url.split("/");
            String folder = parts[parts.length - 2];
            String filename = parts[parts.length - 1];
            String filenameWithoutExt = filename.split("\\.")[0];
            return folder + "/" + filenameWithoutExt;
        } catch (Exception e) {
            log.error("public_id 추출 실패: {}", url, e);
            String[] parts = url.split("/");
            String filename = parts[parts.length - 1];
            return filename.split("\\.")[0];
        }
    }
}