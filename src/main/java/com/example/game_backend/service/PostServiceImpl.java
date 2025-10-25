package com.example.game_backend.service;

import com.example.game_backend.controller.dto.PostRequest;
import com.example.game_backend.repository.CommentLikeRepository;
import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.PostLikeRepository;
import com.example.game_backend.repository.PostRepository;
import com.example.game_backend.repository.entity.Image;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.entity.Post;
import com.example.game_backend.repository.entity.PostLike;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CloudinaryService cloudinaryService;
    private final MemberRepository memberRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Override
    @Transactional
    public Long save(PostRequest request, Member writer) {
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .writer(writer)
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (post.getImages() == null) {
            post.setImages(new ArrayList<>());
        }

        MultipartFile[] files = request.getImageFiles();
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = storeFileAndGetUrl(file);
                    post.getImages().add(
                            Image.builder()
                                    .filePath(url)
                                    .post(post)
                                    .build()
                    );
                }
            }
        }

        return postRepository.save(post).getId();
    }

    @Override
    @Transactional
    public void updatePost(Long postId, PostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setUpdatedAt(LocalDateTime.now());

        List<String> keepUrls = request.getKeepImageUrls() != null
                ? request.getKeepImageUrls()
                : new ArrayList<>();

        post.getImages().removeIf(img -> {
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
                    post.getImages().add(
                            Image.builder()
                                    .filePath(url)
                                    .post(post)
                                    .build()
                    );
                }
            }
        }
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        if (post.getImages() != null) {
            for (Image image : post.getImages()) {
                cloudinaryService.deleteImage(image.getFilePath());
            }
        }

        postRepository.deleteById(postId);
    }

    @Override
    public String storeFileAndGetUrl(MultipartFile file) {
        String uniqueId = "post_" + System.currentTimeMillis();
        return cloudinaryService.uploadImage(file, uniqueId);
    }

    // 게시글 좋아요 토글 (기존)
    @Override
    @Transactional
    public void toggleLike(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        boolean alreadyLiked = postLikeRepository.existsByPostAndMember(post, member);

        if (alreadyLiked) {
            postLikeRepository.deleteByPostAndMember(post, member);
            post.setLikeCount(post.getLikeCount() - 1);
        } else {
            PostLike like = PostLike.builder()
                    .post(post)
                    .member(member)
                    .build();
            postLikeRepository.save(like);
            post.setLikeCount(post.getLikeCount() + 1);
        }

        postRepository.save(post);
    }

    // ⭐ 게시글 좋아요 토글 + 상태 반환 (신규)
    @Override
    @Transactional
    public Map<String, Object> toggleLikeAndGetStatus(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        boolean alreadyLiked = postLikeRepository.existsByPostAndMember(post, member);

        if (alreadyLiked) {
            postLikeRepository.deleteByPostAndMember(post, member);
            post.setLikeCount(post.getLikeCount() - 1);
        } else {
            PostLike like = PostLike.builder()
                    .post(post)
                    .member(member)
                    .build();
            postLikeRepository.save(like);
            post.setLikeCount(post.getLikeCount() + 1);
        }

        postRepository.save(post);

        // 최종 상태 조회
        boolean isLiked = postLikeRepository.existsByPostAndMember(post, member);

        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        result.put("likeCount", post.getLikeCount());

        return result;
    }

    // ⭐ 게시글 좋아요 상태 조회 (신규)
    @Override
    @Transactional(readOnly = true)
    public boolean isPostLikedByUser(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        return postLikeRepository.existsByPostAndMember(post, member);
    }

    // ⭐ 댓글 좋아요 상태 조회 (신규)
    @Override
    @Transactional(readOnly = true)
    public List<Long> getLikedCommentIds(Long postId, String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        return commentLikeRepository.findLikedCommentIdsByPostAndMember(postId, member.getId());
    }
}