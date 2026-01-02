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
public class CacheTourApi {
    private Long cacheId;
    private String contentId;
    private String contentTypeId;
    private String title;
    private String areaCode;
    private String sigunguCode;
    private String cat1;
    private String cat2;
    private String cat3;
    private String addr1;
    private String addr2;
    private String mapX;
    private String mapY;
    private String firstImage;
    private String firstImage2;
    private String overview;
    private Boolean isPetFriendly;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
