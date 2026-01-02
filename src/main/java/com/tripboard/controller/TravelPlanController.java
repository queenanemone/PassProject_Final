package com.tripboard.controller;

import com.tripboard.dto.ApiResponse;
import com.tripboard.dto.TravelPlanRequest;
import com.tripboard.entity.TravelPlan;
import com.tripboard.service.OpenAiService;
import com.tripboard.service.TravelPlanService;
import com.tripboard.util.SecurityUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 여행 계획 관련 REST API Controller
 */
@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class TravelPlanController {

    private final TravelPlanService travelPlanService;
    private final SecurityUtil securityUtil;
    private final OpenAiService openAiService;

    /**
     * 여행 계획 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TravelPlan>> createPlan(@RequestBody TravelPlanRequest request) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            TravelPlan plan = travelPlanService.createPlan(userId, request);
            return ResponseEntity.ok(ApiResponse.success("여행 계획이 생성되었습니다", plan));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.error("서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 여행 계획 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPlan(@PathVariable Long id) {
        try {
            Map<String, Object> details = travelPlanService.getPlanDetails(id);
            return ResponseEntity.ok(ApiResponse.success(details));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.error("서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 여행 계획 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TravelPlan>> updatePlan(@PathVariable Long id,
            @RequestBody TravelPlanRequest request) {
        try {
            TravelPlan updatedPlan = travelPlanService.updatePlan(id, request);
            return ResponseEntity.ok(ApiResponse.success("여행 계획이 수정되었습니다", updatedPlan));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.error("서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 사용자 여행 계획 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TravelPlan>>> getUserPlans() {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            List<TravelPlan> plans = travelPlanService.getUserPlans(userId);
            if (plans == null) {
                plans = new ArrayList<>();
            }
            return ResponseEntity.ok(ApiResponse.success(plans));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.error("서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 여행 계획 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deletePlan(@PathVariable Long id) {
        try {
            travelPlanService.deletePlan(id);
            return ResponseEntity.ok(ApiResponse.success("여행 계획이 삭제되었습니다", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 관광지 항목 삭제
     */
    @DeleteMapping("/spots/{planId}/{spotOrder}")
    public ResponseEntity<Void> deleteSpot(@PathVariable Long planId, @PathVariable int spotOrder) {
        travelPlanService.deleteSpot(planId, spotOrder);
        return ResponseEntity.ok().build();
    }

    /**
     * 교통편 항목 삭제
     */
    @DeleteMapping("/transports/{planId}/{transportOrder}")
    public ResponseEntity<Void> deleteTransport(@PathVariable Long planId, @PathVariable int transportOrder) {
        travelPlanService.deleteTransport(planId, transportOrder);
        return ResponseEntity.ok().build();
    }

    /**
     * 숙소 항목 삭제
     */
    @DeleteMapping("/accommodations/{planId}")
    public ResponseEntity<Void> deleteAccommodation(@PathVariable Long planId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam int accommodationOrder) {
        travelPlanService.deleteAccommodation(planId, checkInDate, accommodationOrder);
        return ResponseEntity.ok().build();
    }

    /**
     * AI 추천 추가
     */
    @PostMapping("/{id}/ai-recommendations/add")
    public ResponseEntity<ApiResponse<?>> addAiRecommendation(
            @PathVariable Long id,
            @RequestBody Map<String, Object> recommendation) {
        try {
            travelPlanService.addAiRecommendationToPlan(id, recommendation);
            return ResponseEntity.ok(ApiResponse.success("AI 추천이 추가되었습니다", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * AI 추천 항목 직접 추가
     */
    @PostMapping("/{id}/ai-recommendations/direct-add")
    public ResponseEntity<ApiResponse<?>> addAiItemDirectly(
            @PathVariable Long id,
            @RequestBody Map<String, Object> itemData) {
        try {
            travelPlanService.addAiItemDirectly(id, itemData);
            return ResponseEntity.ok(ApiResponse.success("AI 추천 항목이 추가되었습니다.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 관광지 검색 (필터 포함)
     */
    @GetMapping("/search/tour")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchTourData(
            @RequestParam String keyword,
            @RequestParam(required = false) String contentTypeId,
            @RequestParam(required = false) String areaCode) {
        try {
            List<Map<String, Object>> results = travelPlanService.searchTourWithFilter(keyword, contentTypeId,
                    areaCode);
            return ResponseEntity.ok(ApiResponse.success(results));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 기차 검색
     */
    @GetMapping("/search/train")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchTrainData(
            @RequestParam String depStation,
            @RequestParam String arrStation,
            @RequestParam String date,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String trainType) {
        try {
            List<Map<String, Object>> results = travelPlanService.searchTrainWithFilter(depStation, arrStation, date,
                    time, trainType);
            return ResponseEntity.ok(ApiResponse.success(results));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 항목 수동 추가
     */
    @PostMapping("/{planId}/add-item")
    public ResponseEntity<ApiResponse<?>> addItemToPlan(
            @PathVariable Long planId,
            @RequestBody Map<String, Object> itemData) {
        try {
            travelPlanService.addManualItem(planId, itemData);
            return ResponseEntity.ok(ApiResponse.success("항목이 추가되었습니다.", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 데이터셋 항목 추가
     */
    @PostMapping("/{id}/dataset/add")
    public ResponseEntity<ApiResponse<?>> addDatasetItem(
            @PathVariable Long id,
            @RequestParam String contentId,
            @RequestParam String category) {
        try {
            travelPlanService.addDatasetItemToPlan(id, contentId, category);
            return ResponseEntity.ok(ApiResponse.success("항목이 추가되었습니다", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * AI로 계획 자동 완성
     */
    @PostMapping("/{id}/fill")
    public ResponseEntity<?> fillPlan(
            @PathVariable Long id,
            @RequestBody(required = false) TravelPlanRequest request) {
        try {
            travelPlanService.fillPlan(id, request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "여행 계획이 업데이트되고 AI가 채워졌습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 빈 여행 계획 생성
     */
    @PostMapping("/create-empty")
    public ResponseEntity<ApiResponse<TravelPlan>> createEmptyPlan() {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            TravelPlan plan = travelPlanService.createEmptyPlan(userId);
            return ResponseEntity.ok(ApiResponse.success("빈 여행 계획이 생성되었습니다", plan));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.error("서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 위치 기반 여행지 추천
     */
    @GetMapping("/recommend-destinations")
    public ResponseEntity<?> recommendDestinations(
            @RequestParam double lat,
            @RequestParam double lon) {

        List<Map<String, String>> recommendations = openAiService.suggestDestinationsByLocation(lat, lon);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", recommendations);

        return ResponseEntity.ok(response);
    }

    /**
     * 여행 계획 제목 수정
     */
    @PutMapping("/{id}/title")
    public ResponseEntity<?> updatePlanTitle(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            String newTitle = payload.get("title");
            if (newTitle == null || newTitle.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("제목을 입력해주세요."));
            }

            travelPlanService.updateTitle(id, newTitle);

            return ResponseEntity.ok(ApiResponse.success("제목이 수정되었습니다.", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 일정 항목 이동
     */
    @PutMapping("/{id}/move-item")
    public ResponseEntity<?> moveItem(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        try {
            String type = (String) payload.get("type");
            String newDate = (String) payload.get("newDate");
            Map<String, Object> idData = (Map<String, Object>) payload.get("idData");

            travelPlanService.moveItem(id, type, idData, newDate);
            return ResponseEntity.ok(ApiResponse.success("항목이 이동되었습니다", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

}