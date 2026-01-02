package com.tripboard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotPlace {
    private Long hotplaceId;
    private Long userId;
    private String title;
    private String description;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String contentId; // 한국관광공사 API content_id (선택)
    private String contentTypeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // 작성자 정보 (JOIN으로 가져옴)
    private String authorNickname;
    private String authorName;
    private String authorProfileImage;
}

