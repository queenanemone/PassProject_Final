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
public class AiRecommendation {
    private Long recommendationId;
    private Long planId;
    private Long userId;
    private String recommendationType;
    private String contentId;
    private String title;
    private String description;
    private String reason;
    private String address;
    private String mapX;
    private String mapY;
    private String imageKeyword;
    private LocalDateTime createdAt;
}
