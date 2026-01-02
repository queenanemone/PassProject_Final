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
public class HotPlaceImage {
    private Long imageId;
    private Long hotplaceId;
    private String imageUrl;
    private Integer imageOrder;
    private LocalDateTime createdAt;
}

