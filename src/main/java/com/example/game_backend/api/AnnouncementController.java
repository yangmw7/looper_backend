package com.example.game_backend.api;

import com.example.game_backend.config.JwtUtil;
import com.example.game_backend.controller.dto.announcement.AnnouncementRequest;
import com.example.game_backend.controller.dto.announcement.AnnouncementResponse;
import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.entity.Announcement;
import com.example.game_backend.repository.entity.AnnouncementCategory;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.AnnouncementRepository;
import com.example.game_backend.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final AnnouncementRepository announcementRepository;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    /**
     * 공지사항 작성 (관리자만)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> createAnnouncement(
            @ModelAttribute AnnouncementRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Long id = announcementService.save(request, member);
        return ResponseEntity.ok(id);
    }

    /**
     * 공지사항 목록 조회 (핀 포함, 자동 정렬)
     * 정렬: 핀 우선 → 최신순
     */
    @GetMapping
    public ResponseEntity<Page<AnnouncementResponse>> getAll(
            @RequestParam(required = false) AnnouncementCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AnnouncementResponse> announcements = announcementService.getAll(category, pageable);

        return ResponseEntity.ok(announcements);
    }

    /**
     * 공지사항 단건 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementResponse> getOne(@PathVariable Long id) {
        AnnouncementResponse response = announcementService.getOne(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 공지사항 수정 (관리자만)
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateAnnouncement(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("category") AnnouncementCategory category,
            @RequestParam(value = "keepImageUrls", required = false) List<String> keepImageUrls,
            @RequestPart(value = "imageFiles", required = false) MultipartFile[] imageFiles,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Announcement existing = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항 없음"));

        if (!existing.getWriter().getId().equals(member.getId())) {
            return ResponseEntity.status(403).body("권한 없음");
        }

        AnnouncementRequest req = new AnnouncementRequest();
        req.setTitle(title);
        req.setContent(content);
        req.setCategory(category);
        req.setKeepImageUrls(keepImageUrls);
        req.setImageFiles(imageFiles);

        announcementService.updateAnnouncement(id, req);
        return ResponseEntity.ok("수정 완료");
    }

    /**
     * 공지사항 삭제 (관리자만)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Announcement existing = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항 없음"));

        if (!existing.getWriter().getId().equals(member.getId())) {
            return ResponseEntity.status(403).build();
        }

        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 공지사항 좋아요 토글
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> toggleLike(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);

        announcementService.toggleLike(id, username);
        return ResponseEntity.ok().build();
    }


    /**
     * 공지사항 핀 토글 (관리자만)
     * - 최대 3개까지 핀 가능
     * - 3개 초과 시 가장 오래된 핀 자동 해제 (FIFO)
     */
    @PostMapping("/{id}/pin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> togglePin(@PathVariable Long id) {
        announcementService.togglePin(id);
        return ResponseEntity.ok().build();
    }
}