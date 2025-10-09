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

    /**
     * 이미지를 Cloudinary에 업로드하고 URL 반환
     */
    public String uploadImage(MultipartFile file, String publicId) {
        try {
            // ✅ public_id 접두어에 따라 폴더 동적 분기
            String folder;
            if (publicId.startsWith("post_")) {
                folder = "looper-posts";
            } else if (publicId.startsWith("npc_")) {
                folder = "looper-npcs";
            } else {
                folder = "looper-items";
            }

            // 파일 업로드
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,            // 동적 폴더 이름
                            "public_id", publicId,       // 파일 ID
                            "overwrite", true,           // 같은 ID면 덮어쓰기
                            "resource_type", "auto"      // 자동 타입 감지
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

    /**
     * Cloudinary에서 이미지 삭제
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return;
        }

        try {
            // URL에서 폴더와 public_id 추출
            String fullPublicId = extractFullPublicId(imageUrl);

            cloudinary.uploader().destroy(fullPublicId, ObjectUtils.emptyMap());

            log.info("이미지 삭제 성공: {}", fullPublicId);

        } catch (IOException e) {
            log.error("이미지 삭제 실패: {}", e.getMessage());
        }
    }

    /**
     * URL에서 폴더명 포함 public_id 추출
     * 예: https://res.cloudinary.com/.../looper-items/item123.jpg -> looper-items/item123
     */
    private String extractFullPublicId(String url) {
        try {
            // URL을 '/'로 분리
            String[] parts = url.split("/");

            // 마지막 2개 부분 추출 (폴더명/파일명)
            String folder = parts[parts.length - 2];
            String filename = parts[parts.length - 1];

            // 확장자 제거
            String filenameWithoutExt = filename.split("\\.")[0];

            return folder + "/" + filenameWithoutExt;
        } catch (Exception e) {
            log.error("public_id 추출 실패: {}", url, e);
            // 실패 시 파일명만 반환 (하위 호환)
            String[] parts = url.split("/");
            String filename = parts[parts.length - 1];
            return filename.split("\\.")[0];
        }
    }
}
