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
public class PlanTransport {
    private Long planId;
    private Integer transportOrder;
    private String transportType;
    private String departureStationCode;
    private String arrivalStationCode;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer price;
    private String reservationUrl;
    private Boolean isSelected;
    private LocalDateTime createdAt;
}
