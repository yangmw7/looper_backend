package com.example.game_backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPageResponseDto {

    private ProfileResponseDto profile;
    private StatsResponseDto stats;

    // TODO: 나중에 추가할 필드들
    // private List<PostSummaryDto> recentPosts;
    // private List<CommentSummaryDto> recentComments;
}