package com.tripboard.controller;

import com.tripboard.dto.ApiResponse;
import com.tripboard.dto.HotPlaceRequest;
import com.tripboard.entity.HotPlace;
import com.tripboard.entity.HotPlaceImage;
import com.tripboard.service.HotPlaceService;
import com.tripboard.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HotPlace 관련 REST API Controller
 */
@RestController
@RequestMapping("/api/hotplaces")
@RequiredArgsConstructor
public class HotPlaceController {

    private final HotPlaceService hotPlaceService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HotPlace>>> getAllHotPlaces() {
        try {
            List<HotPlace> hotPlaces = hotPlaceService.getAllHotPlaces();
            return ResponseEntity.ok(ApiResponse.success(hotPlaces));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<HotPlace>>> getMyHotPlaces() {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            List<HotPlace> hotPlaces = hotPlaceService.getUserHotPlaces(userId);
            return ResponseEntity.ok(ApiResponse.success(hotPlaces));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HotPlace>> getHotPlace(@PathVariable Long id) {
        try {
            HotPlace hotPlace = hotPlaceService.getHotPlace(id);
            if (hotPlace == null) {
                return ResponseEntity.status(404).body(ApiResponse.error("HotPlace를 찾을 수 없습니다"));
            }
            return ResponseEntity.ok(ApiResponse.success(hotPlace));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HotPlace>> createHotPlace(@RequestBody HotPlaceRequest request) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("제목을 입력해주세요"));
            }

            if (request.getLatitude() == null || request.getLongitude() == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("위도와 경도를 입력해주세요"));
            }

            // 좌표 유효성 검사 추가
            if (request.getLatitude() < -90 || request.getLatitude() > 90) {
                return ResponseEntity.badRequest().body(ApiResponse.error("유효하지 않은 위도입니다 (-90 ~ 90)"));
            }
            if (request.getLongitude() < -180 || request.getLongitude() > 180) {
                return ResponseEntity.badRequest().body(ApiResponse.error("유효하지 않은 경도입니다 (-180 ~ 180)"));
            }

            HotPlace hotPlace = hotPlaceService.createHotPlace(userId, request);
            return ResponseEntity.ok(ApiResponse.success("HotPlace가 등록되었습니다", hotPlace));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error("등록 실패: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<?>> addImage(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> body) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            HotPlace hotPlace = hotPlaceService.getHotPlace(id);
            if (hotPlace == null) {
                return ResponseEntity.status(404).body(ApiResponse.error("HotPlace를 찾을 수 없습니다"));
            }

            if (!hotPlace.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body(ApiResponse.error("본인이 등록한 HotPlace에만 이미지를 추가할 수 있습니다"));
            }

            String imageUrl = (String) body.get("imageUrl");
            Integer imageOrder = body.get("imageOrder") != null 
                    ? ((Number) body.get("imageOrder")).intValue() : 0;

            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("이미지 URL을 입력해주세요"));
            }

            hotPlaceService.addImage(id, imageUrl, imageOrder);
            return ResponseEntity.ok(ApiResponse.success("이미지가 추가되었습니다", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<ApiResponse<List<HotPlaceImage>>> getHotPlaceImages(@PathVariable Long id) {
        try {
            List<HotPlaceImage> images = hotPlaceService.getHotPlaceImages(id);
            return ResponseEntity.ok(ApiResponse.success(images));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HotPlace>> updateHotPlace(
            @PathVariable Long id,
            @RequestBody HotPlaceRequest request) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            HotPlace hotPlace = hotPlaceService.getHotPlace(id);
            if (hotPlace == null) {
                return ResponseEntity.status(404).body(ApiResponse.error("HotPlace를 찾을 수 없습니다"));
            }

            if (!hotPlace.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body(ApiResponse.error("본인이 등록한 HotPlace만 수정할 수 있습니다"));
            }

            hotPlaceService.updateHotPlace(id, request);
            HotPlace updatedHotPlace = hotPlaceService.getHotPlace(id);
            return ResponseEntity.ok(ApiResponse.success("HotPlace가 수정되었습니다", updatedHotPlace));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteHotPlace(@PathVariable Long id) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            HotPlace hotPlace = hotPlaceService.getHotPlace(id);
            if (hotPlace == null) {
                return ResponseEntity.status(404).body(ApiResponse.error("HotPlace를 찾을 수 없습니다"));
            }

            if (!hotPlace.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body(ApiResponse.error("본인이 등록한 HotPlace만 삭제할 수 있습니다"));
            }

            hotPlaceService.deleteHotPlace(id);
            return ResponseEntity.ok(ApiResponse.success("HotPlace가 삭제되었습니다", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

