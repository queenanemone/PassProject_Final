package com.tripboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PdfExportService {
    
    private final TravelPlanService travelPlanService;
    
    /**
     * 여행 계획을 PDF로 내보내기 위한 데이터 준비
     * 실제 PDF 생성은 프론트엔드에서 jsPDF 같은 라이브러리 사용
     */
    public Map<String, Object> preparePdfData(Long planId) {
        return travelPlanService.getPlanDetails(planId);
    }
}

