package com.tripboard.controller;

import com.tripboard.dto.ApiResponse;
import com.tripboard.dto.ChangePasswordRequest;
import com.tripboard.entity.User;
import com.tripboard.entity.UserTravelPreference;
import com.tripboard.service.UserService;
import com.tripboard.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityUtil securityUtil;

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<User>> getUserInfo() {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            User user = userService.getUserInfo(userId);
            if (user == null) {
                return ResponseEntity.status(404).body(ApiResponse.error("사용자를 찾을 수 없습니다"));
            }

            user.setPassword(null);

            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("사용자 정보 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/preference")
    public ResponseEntity<ApiResponse<UserTravelPreference>> getUserPreference() {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            UserTravelPreference preference = userService.getUserPreference(userId);
            return ResponseEntity.ok(ApiResponse.success(preference));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("여행 성향 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @PostMapping("/preference")
    public ResponseEntity<ApiResponse<UserTravelPreference>> saveUserPreference(
            @RequestBody UserTravelPreference request) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            UserTravelPreference preference = userService.saveOrUpdatePreference(userId, request);
            return ResponseEntity.ok(ApiResponse.success("여행 성향이 저장되었습니다", preference));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("여행 성향 저장 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @PutMapping("/nickname")
    public ResponseEntity<ApiResponse<User>> updateNickname(@RequestBody java.util.Map<String, String> request) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            String nickname = request.get("nickname");
            if (nickname == null || nickname.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("닉네임을 입력해주세요"));
            }

            User user = userService.updateNickname(userId, nickname.trim());
            user.setPassword(null);

            return ResponseEntity.ok(ApiResponse.success("닉네임이 변경되었습니다", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("닉네임 변경 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @PutMapping("/profile-image")
    public ResponseEntity<ApiResponse<User>> updateProfileImage(@RequestBody java.util.Map<String, String> request) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            String profileImage = request.get("profileImage");
            if (profileImage == null || profileImage.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("프로필 이미지를 입력해주세요"));
            }

            if (profileImage.length() > 60000) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("이미지 파일이 너무 큽니다. 이미지를 압축하거나 더 작은 크기의 이미지를 선택해주세요. (최대 50KB 원본 이미지)"));
            }

            User user = userService.updateProfileImage(userId, profileImage.trim());
            user.setPassword(null);

            return ResponseEntity.ok(ApiResponse.success("프로필 이미지가 변경되었습니다", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("프로필 이미지 변경 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @PutMapping("/bio")
    public ResponseEntity<ApiResponse<User>> updateBio(@RequestBody java.util.Map<String, String> request) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            String bio = request.get("bio");
            // 빈 문자열도 허용 (삭제의 경우)

            User user = userService.updateBio(userId, bio);
            user.setPassword(null);

            return ResponseEntity.ok(ApiResponse.success("자기소개가 변경되었습니다", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("자기소개 변경 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<?>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("현재 비밀번호를 입력해주세요"));
            }

            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("새 비밀번호를 입력해주세요"));
            }

            if (request.getNewPassword().length() < 8) {
                return ResponseEntity.badRequest().body(ApiResponse.error("새 비밀번호는 8자 이상이어야 합니다"));
            }

            userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("비밀번호 변경 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> deleteUser() {
        try {
            Long userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다"));
            }

            userService.deleteUser(userId);
            return ResponseEntity.ok(ApiResponse.success("회원탈퇴가 완료되었습니다", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("회원탈퇴 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
