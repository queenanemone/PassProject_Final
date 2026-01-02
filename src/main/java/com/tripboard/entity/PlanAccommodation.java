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
public class PlanAccommodation {
    private Long planId;
    private LocalDate checkInDate;
    private Integer accommodationOrder;
    private String contentId;
    private LocalDate checkOutDate;
    private Boolean isSelected;
    private LocalDateTime createdAt;
}
