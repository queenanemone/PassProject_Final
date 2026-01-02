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
public class Bookmark {
    private Long bookmarkId;
    private Long userId;
    private Long planId;
    private Long postId;
    private LocalDateTime createdAt;
}

