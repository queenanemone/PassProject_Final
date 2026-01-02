package com.tripboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotPlaceRequest {
    private String title;
    private String description;
    private String address;
    private Double latitude;  // JSON에서 숫자로 받고 서비스에서 BigDecimal로 변환
    private Double longitude; // JSON에서 숫자로 받고 서비스에서 BigDecimal로 변환
    private String contentId;
    private String contentTypeId;
    
    // BigDecimal getter 추가 (서비스에서 사용)
    public BigDecimal getLatitudeAsBigDecimal() {
        return latitude != null ? BigDecimal.valueOf(latitude) : null;
    }
    
    public BigDecimal getLongitudeAsBigDecimal() {
        return longitude != null ? BigDecimal.valueOf(longitude) : null;
    }
}

