package com.tripboard.controller;

import com.tripboard.dto.ApiResponse;
import com.tripboard.dto.ForgotPasswordRequest;
import com.tripboard.dto.GoogleLoginRequest;
import com.tripboard.dto.LoginRequest;
import com.tripboard.dto.RegisterRequest;
import com.tripboard.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 REST API Controller
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            var result = authService.register(request);
            return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다", result));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("이미 존재")) {
                return ResponseEntity.status(409).body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("회원가입 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {
        try {
            var result = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("로그인 성공", result));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("이메일") || e.getMessage().contains("비밀번호") || e.getMessage().contains("비활성화")) {
                return ResponseEntity.status(401).body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("로그인 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/google/client-id")
    public ResponseEntity<ApiResponse<String>> getGoogleClientId() {
        try {
            String clientId = authService.getGoogleClientId();
            return ResponseEntity.ok(ApiResponse.success("Google Client ID", clientId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Client ID를 가져올 수 없습니다: " + e.getMessage()));
        }
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<?>> googleLogin(@RequestBody GoogleLoginRequest request) {
        try {
            var result = authService.googleLogin(
                    request.getCredential(),
                    request.getEmail(),
                    request.getName(),
                    request.getPicture());
            return ResponseEntity.ok(ApiResponse.success("Google 로그인 성공", result));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("비활성화")) {
                return ResponseEntity.status(401).body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Google 로그인 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request.getEmail());
            // 보안을 위해 임시 비밀번호를 반환하지 않고, 이메일로만 발송
            return ResponseEntity.ok(ApiResponse.success("임시 비밀번호가 생성되어 이메일로 발송되었습니다. 이메일을 확인해주세요.", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("비밀번호 재설정 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
