package com.tripboard.controller;

import com.tripboard.dto.ApiResponse;
import com.tripboard.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PDF 내보내기 관련 REST API Controller
 * Spring Boot 표준 구조
 */
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class PdfExportController {
    
    private final PdfExportService pdfExportService;
    
    @GetMapping("/pdf/{planId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPdfData(@PathVariable Long planId) {
        try {
            Map<String, Object> pdfData = pdfExportService.preparePdfData(planId);
            return ResponseEntity.ok(ApiResponse.success(pdfData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

