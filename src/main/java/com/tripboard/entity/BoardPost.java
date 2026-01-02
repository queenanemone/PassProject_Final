package com.tripboard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardPost {
    private Long postId;
    private Long userId;
    private Long planId;
    private Long hotplaceId;
    private String title;
    private String content;
    private String regionCode;
    private String tripType;
    private String season;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount; // 댓글 수
    private Boolean isPublic;
    private String category; // "TRAVEL_PLAN" (국내 여행 공유) 또는 "TRAVEL_RECORD" (여행 기록)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // 작성자 정보 (JOIN으로 가져옴)
    private String authorNickname;
    private String authorName;
    private String authorProfileImage;
    // 현재 사용자가 좋아요를 눌렀는지 여부 (조회 시 설정)
    private Boolean liked;
}

