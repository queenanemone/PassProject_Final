package com.tripboard.controller;

import com.tripboard.dto.ApiResponse;
import com.tripboard.dto.RecordImageRequest;
import com.tripboard.dto.RecordRequest;
import com.tripboard.entity.RecordImage;
import com.tripboard.entity.TravelRecord;
import com.tripboard.service.TravelRecordService;
import com.tripboard.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 여행 기록 관련 REST API Controller
 */
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class TravelRecordController {

    private final TravelRecordService travelRecordService;
    private final SecurityUtil securityUtil;

    @PostMapping
    public ResponseEntity<ApiResponse<TravelRecord>> createRecord(@RequestBody RecordRequest request) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            TravelRecord record = travelRecordService.createRecord(
                    userId,
                    request.getPlanId(),
                    request.getTitle(),
                    request.getContent());
            return ResponseEntity.ok(ApiResponse.success("여행 기록이 생성되었습니다", record));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error("여행 기록 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @PostMapping("/images")
    public ResponseEntity<ApiResponse<?>> addImage(@RequestBody RecordImageRequest request) {
        try {
            travelRecordService.addImage(
                    request.getRecordId(),
                    request.getImageUrl(),
                    request.getImageOrder());
            return ResponseEntity.ok(ApiResponse.success("이미지가 추가되었습니다", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error("이미지 추가 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<TravelRecord>>> getUserRecords() {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            List<TravelRecord> records = travelRecordService.getUserRecords(userId);
            return ResponseEntity.ok(ApiResponse.success(records));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/plan/{planId}")
    public ResponseEntity<ApiResponse<List<TravelRecord>>> getRecordsByPlan(@PathVariable Long planId) {
        try {
            List<TravelRecord> records = travelRecordService.getRecordsByPlanId(planId);
            return ResponseEntity.ok(ApiResponse.success(records));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TravelRecord>> getRecord(@PathVariable Long id) {
        try {
            TravelRecord record = travelRecordService.getRecord(id);
            return ResponseEntity.ok(ApiResponse.success(record));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<ApiResponse<List<RecordImage>>> getRecordImages(@PathVariable Long id) {
        try {
            List<RecordImage> images = travelRecordService.getRecordImages(id);
            return ResponseEntity.ok(ApiResponse.success(images));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error("이미지 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateRecord(
            @PathVariable Long id,
            @RequestBody RecordRequest request) {
        try {
            travelRecordService.updateRecord(id, request.getTitle(), request.getContent());
            return ResponseEntity.ok(ApiResponse.success("여행 기록이 수정되었습니다", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteRecord(@PathVariable Long id) {
        try {
            travelRecordService.deleteRecord(id);
            return ResponseEntity.ok(ApiResponse.success("여행 기록이 삭제되었습니다", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
