package com.example.game_backend.service;

import com.example.game_backend.controller.dto.announcement.AnnouncementRequest;
import com.example.game_backend.controller.dto.announcement.AnnouncementResponse;
import com.example.game_backend.repository.*;
import com.example.game_backend.repository.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementLikeRepository announcementLikeRepository;
    private final MemberRepository memberRepository;
    private final CloudinaryService cloudinaryService;

    private static final int MAX_PINNED = 3;

    @Override
    @Transactional
    public Long save(AnnouncementRequest request, Member writer) {
        log.info("[공지사항 작성] 제목: {}, 작성자: {}", request.getTitle(), writer.getUsername());

        Boolean isPinned = request.getIsPinned() != null ? request.getIsPinned() : false;
        LocalDateTime pinnedAt = null;

        if (isPinned) {
            long currentPinnedCount = announcementRepository.countByIsPinnedTrue();

            if (currentPinnedCount >= MAX_PINNED) {
                Announcement oldestPinned = announcementRepository.findFirstByIsPinnedTrueOrderByPinnedAtAsc();
                if (oldestPinned != null) {
                    oldestPinned.setIsPinned(false);
                    oldestPinned.setPinnedAt(null);
                    announcementRepository.save(oldestPinned);
                    log.info("가장 오래된 핀 자동 해제 - ID: {}", oldestPinned.getId());
                }
            }

            pinnedAt = LocalDateTime.now();
        }

        Announcement announcement = Announcement.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .writer(writer)
                .viewCount(0)
                .likeCount(0)
                .isPinned(isPinned)
                .pinnedAt(pinnedAt)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (announcement.getImages() == null) {
            announcement.setImages(new ArrayList<>());
        }

        MultipartFile[] files = request.getImageFiles();
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = storeFileAndGetUrl(file);
                    announcement.getImages().add(
                            AnnouncementImage.builder()
                                    .filePath(url)
                                    .announcement(announcement)
                                    .build()
                    );
                }
            }
        }

        Long savedId = announcementRepository.save(announcement).getId();
        log.info("공지사항 작성 완료 - ID: {}, 핀 설정: {}", savedId, isPinned);
        return savedId;
    }

    @Override
    @Transactional
    public void updateAnnouncement(Long id, AnnouncementRequest request, String editorUsername) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항 없음"));

        log.info("[공지사항 수정] ID: {}, 제목: {}, 원작성자: {}, 수정자: {}",
                id,
                announcement.getTitle(),
                announcement.getWriter().getUsername(),
                editorUsername
        );

        Boolean newIsPinned = request.getIsPinned() != null ? request.getIsPinned() : false;
        Boolean oldIsPinned = announcement.getIsPinned();

        if (newIsPinned && !oldIsPinned) {
            long currentPinnedCount = announcementRepository.countByIsPinnedTrue();

            if (currentPinnedCount >= MAX_PINNED) {
                Announcement oldestPinned = announcementRepository.findFirstByIsPinnedTrueOrderByPinnedAtAsc();
                if (oldestPinned != null && !oldestPinned.getId().equals(id)) {
                    oldestPinned.setIsPinned(false);
                    oldestPinned.setPinnedAt(null);
                    announcementRepository.save(oldestPinned);
                    log.info("가장 오래된 핀 자동 해제 - ID: {}", oldestPinned.getId());
                }
            }

            announcement.setIsPinned(true);
            announcement.setPinnedAt(LocalDateTime.now());
            log.info("핀 설정 - ID: {}", id);
        } else if (!newIsPinned && oldIsPinned) {
            announcement.setIsPinned(false);
            announcement.setPinnedAt(null);
            log.info("핀 해제 - ID: {}", id);
        }

        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setCategory(request.getCategory());
        announcement.setUpdatedAt(LocalDateTime.now());

        List<String> keepUrls = request.getKeepImageUrls() != null
                ? request.getKeepImageUrls()
                : new ArrayList<>();

        announcement.getImages().removeIf(img -> {
            if (!keepUrls.contains(img.getFilePath())) {
                cloudinaryService.deleteImage(img.getFilePath());
                return true;
            }
            return false;
        });

        MultipartFile[] newFiles = request.getImageFiles();
        if (newFiles != null) {
            for (MultipartFile f : newFiles) {
                if (!f.isEmpty()) {
                    String url = storeFileAndGetUrl(f);
                    announcement.getImages().add(
                            AnnouncementImage.builder()
                                    .filePath(url)
                                    .announcement(announcement)
                                    .build()
                    );
                }
            }
        }

        log.info("공지사항 수정 완료 - ID: {}", id);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long id, String deleterUsername) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항 없음"));

        log.warn("[공지사항 삭제] ID: {}, 제목: {}, 원작성자: {}, 삭제자: {}",
                id,
                announcement.getTitle(),
                announcement.getWriter().getUsername(),
                deleterUsername
        );

        if (announcement.getImages() != null) {
            for (AnnouncementImage image : announcement.getImages()) {
                cloudinaryService.deleteImage(image.getFilePath());
            }
        }

        announcementRepository.deleteById(id);
        log.info("공지사항 삭제 완료 - ID: {}", id);
    }

    @Override
    public String storeFileAndGetUrl(MultipartFile file) {
        String uniqueId = "announcement_" + System.currentTimeMillis();
        return cloudinaryService.uploadImage(file, uniqueId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnnouncementResponse> getAll(AnnouncementCategory category, Pageable pageable) {
        Page<Announcement> page = (category == null)
                ? announcementRepository.findAllOrderByPinned(pageable)
                : announcementRepository.findByCategoryOrderByPinned(category, pageable);

        return page.map(this::toResponse);
    }

    @Override
    @Transactional
    public AnnouncementResponse getOne(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항 없음"));

        announcement.increaseViewCount();
        announcementRepository.save(announcement);

        return toResponse(announcement);
    }

    @Override
    @Transactional
    public void toggleLike(Long announcementId, String username) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new RuntimeException("공지사항 없음"));

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        boolean alreadyLiked = announcementLikeRepository.existsByAnnouncementAndMember(announcement, member);

        if (alreadyLiked) {
            announcementLikeRepository.deleteByAnnouncementAndMember(announcement, member);
            announcement.setLikeCount(announcement.getLikeCount() - 1);
        } else {
            AnnouncementLike like = AnnouncementLike.builder()
                    .announcement(announcement)
                    .member(member)
                    .build();
            announcementLikeRepository.save(like);
            announcement.setLikeCount(announcement.getLikeCount() + 1);
        }

        announcementRepository.save(announcement);
    }

    @Override
    @Transactional
    public void togglePin(Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new RuntimeException("공지사항 없음"));

        if (announcement.getIsPinned()) {
            announcement.setIsPinned(false);
            announcement.setPinnedAt(null);
            log.info("핀 해제 - ID: {}", announcementId);
        } else {
            long currentPinnedCount = announcementRepository.countByIsPinnedTrue();

            if (currentPinnedCount >= MAX_PINNED) {
                Announcement oldestPinned = announcementRepository.findFirstByIsPinnedTrueOrderByPinnedAtAsc();
                if (oldestPinned != null) {
                    oldestPinned.setIsPinned(false);
                    oldestPinned.setPinnedAt(null);
                    announcementRepository.save(oldestPinned);
                    log.info("가장 오래된 핀 자동 해제 - ID: {}", oldestPinned.getId());
                }
            }

            announcement.setIsPinned(true);
            announcement.setPinnedAt(LocalDateTime.now());
            log.info("핀 설정 - ID: {}", announcementId);
        }

        announcementRepository.save(announcement);
    }

    private AnnouncementResponse toResponse(Announcement a) {
        List<String> urls = a.getImages().stream()
                .map(AnnouncementImage::getFilePath)
                .collect(Collectors.toList());

        return AnnouncementResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .category(a.getCategory())
                .writer(a.getWriter().getNickname())
                .viewCount(a.getViewCount())
                .likeCount(a.getLikeCount())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .imageUrls(urls)
                .commentCount((long) a.getComments().size())
                .isPinned(a.getIsPinned())
                .build();
    }
}