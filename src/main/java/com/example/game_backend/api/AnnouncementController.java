package com.example.game_backend.api;

import com.example.game_backend.config.JwtUtil;
import com.example.game_backend.controller.dto.announcement.AnnouncementRequest;
import com.example.game_backend.controller.dto.announcement.AnnouncementResponse;
import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.entity.AnnouncementCategory;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.AnnouncementRepository;
import com.example.game_backend.security.Role;
import com.example.game_backend.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @GetMapping
    public ResponseEntity<Page<AnnouncementResponse>> getAll(
            @RequestParam(required = false) AnnouncementCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AnnouncementResponse> announcements = announcementService.getAll(category, pageable);

        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementResponse> getOne(@PathVariable Long id) {
        AnnouncementResponse response = announcementService.getOne(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateAnnouncement(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("category") AnnouncementCategory category,
            @RequestParam(value = "isPinned", required = false, defaultValue = "false") Boolean isPinned,
            @RequestParam(value = "keepImageUrls", required = false) List<String> keepImageUrls,
            @RequestPart(value = "imageFiles", required = false) MultipartFile[] imageFiles,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        if (member.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body("관리자 권한이 필요합니다.");
        }

        AnnouncementRequest req = new AnnouncementRequest();
        req.setTitle(title);
        req.setContent(content);
        req.setCategory(category);
        req.setIsPinned(isPinned);
        req.setKeepImageUrls(keepImageUrls);
        req.setImageFiles(imageFiles);

        announcementService.updateAnnouncement(id, req, username);
        return ResponseEntity.ok("수정 완료");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        if (member.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }

        announcementService.deleteAnnouncement(id, username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> toggleLike(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);

        announcementService.toggleLike(id, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/pin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> togglePin(@PathVariable Long id) {
        announcementService.togglePin(id);
        return ResponseEntity.ok().build();
    }
}