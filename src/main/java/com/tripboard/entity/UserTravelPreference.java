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
public class UserTravelPreference {
    private Long preferenceId;
    private Long userId;
    private String preferredTripType; // 가족여행, 커플여행, 혼자여행, 우정여행 등
    private String preferredActivities; // 휴양, 액티비티, 문화, 먹방 등 (JSON 배열 문자열)
    private String budgetPreference; // 절약형, 보통, 여유형
    private String accommodationPreference; // 호텔, 펜션, 게스트하우스, 캠핑 등
    private String seasonPreference; // 봄, 여름, 가을, 겨울, 상관없음
    private String transportationPreference; // 자가용, 대중교통, 렌트카 등
    private String foodPreference; // 한식, 양식, 중식, 일식, 다양하게 등
    private String travelStyle; // 계획형, 즉흥형, 둘 다
    private String additionalInfo; // 추가 정보 (자유 텍스트)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

