package com.tripboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelPlanRequest {
    private String title;
    private String departureRegionCode;
    private String arrivalRegionCode;
    private LocalDate departureDate;
    private LocalDate arrivalDate;
    private Integer adultCount;
    private Integer childCount;
    private Boolean hasPet;
    private Long templateId;
}

