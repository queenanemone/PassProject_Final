package com.tripboard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanSpot {
    private Long planId;
    private Integer spotOrder;
    private String contentId;
    private LocalDate visitDate;
    private LocalTime visitTime;
    private Integer duration;
    private Boolean isSelected;
    private LocalDateTime createdAt;
}
