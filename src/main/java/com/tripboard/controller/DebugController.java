package com.tripboard.controller;

import com.tripboard.entity.CacheTourApi;
import com.tripboard.service.KoreanTourApiService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 진단용 컨트롤러: 서버에서 한국관광공사 API 호출이 동작하는지 간단히 확인할 수 있습니다.
 * 개발 환경에서만 사용하시고 운영에 노출하지 마세요.
 */
@RestController
@RequestMapping("/api/debug/tour")
@RequiredArgsConstructor
public class DebugController {
    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);

    private final KoreanTourApiService koreanTourApiService;

    @GetMapping("/test")
    public ResponseEntity<?> test(
            @RequestParam(name = "operation", required = false, defaultValue = "spots") String operation,
            @RequestParam(name = "areaCode", required = false, defaultValue = "1") String areaCode,
            @RequestParam(name = "hasPet", required = false, defaultValue = "false") boolean hasPet) {
        try {
            logger.info("DebugController.test called - operation={}, areaCode={}, hasPet={}", operation, areaCode,
                    hasPet);

            List<CacheTourApi> results;
            switch (operation) {
                case "accommodations":
                    results = koreanTourApiService.fetchAndCacheAccommodations(areaCode, hasPet);
                    break;
                case "restaurants":
                    results = koreanTourApiService.fetchAndCacheRestaurants(areaCode);
                    break;
                default:
                    results = koreanTourApiService.fetchAndCacheTouristSpots(areaCode, hasPet);
                    break;
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("operation", operation);
            resp.put("areaCode", areaCode);
            resp.put("count", results != null ? results.size() : 0);

            resp.put("items", results == null ? null : results.stream().limit(10).map(r -> {
                Map<String, Object> m = new HashMap<>();
                m.put("contentId", r.getContentId());
                m.put("title", r.getTitle());
                m.put("firstImage", r.getFirstImage());
                m.put("areaCode", r.getAreaCode());
                return m;
            }).toList());

            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            logger.error("DebugController.test 예외: {}", e.getMessage(), e);
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.status(500).body(err);
        }
    }
}
