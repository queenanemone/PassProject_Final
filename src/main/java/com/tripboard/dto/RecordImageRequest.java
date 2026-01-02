package com.tripboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordImageRequest {
    private Long recordId;
    private String imageUrl;
    private Integer imageOrder;
}

