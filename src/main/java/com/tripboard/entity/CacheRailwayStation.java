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
public class CacheRailwayStation {
    // DB Primary Key
    private String stationCode;

    // DB Unique Key
    private String stationName;

    private String cityCode;
    private String cityName;
    private String lineName;
    private String regionCode;
    private LocalDateTime cachedAt;
}