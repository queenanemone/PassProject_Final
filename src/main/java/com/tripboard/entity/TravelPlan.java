package com.tripboard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelPlan {
    private Long planId;
    private Long userId;
    private String title;
    private String departureRegionCode;
    private String arrivalRegionCode;
    private LocalDate departureDate;
    private LocalDate arrivalDate;
    private Integer adultCount;
    private Integer childCount;
    private Boolean hasPet;
    private Long templateId;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

