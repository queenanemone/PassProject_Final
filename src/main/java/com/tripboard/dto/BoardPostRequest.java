package com.tripboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardPostRequest {
    private Long planId;
    private Long hotplaceId;
    private String title;
    private String content;
    private String regionCode;
    private String tripType;
    private String season;
    private String category;
}

