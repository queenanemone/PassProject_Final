package com.tripboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginRequest {
    private String credential; // Google JWT credential 또는 base64 인코딩된 사용자 정보
    private String email; // OAuth 2.0 토큰 방식에서 직접 전달되는 사용자 정보
    private String name;
    private String picture;
}

