package com.tripboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripboard.config.JwtTokenProvider;
import com.tripboard.dto.LoginRequest;
import com.tripboard.dto.RegisterRequest;
import com.tripboard.entity.User;
import com.tripboard.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    @Value("${google.oauth.client-id:}")
    private String googleClientId;

    /**
     * 회원 가입
     */
    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        User existingUser = userMapper.findByEmail(request.getEmail());
        if (existingUser != null) {
            if (existingUser.getIsActive()) {
                throw new RuntimeException("이미 존재하는 이메일입니다");
            } else {
                // 탈퇴한 회원(Soft Deleted) 재가입 처리 (계정 복구)
                existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
                existingUser.setName(request.getName());
                existingUser.setNickname(request.getNickname() != null ? request.getNickname() : request.getName());
                existingUser.setPhone(request.getPhone());
                existingUser.setIsActive(true); // 복구
                existingUser.setUpdatedAt(LocalDateTime.now());

                userMapper.update(existingUser);

                String token = jwtTokenProvider.generateToken(existingUser.getEmail());
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("user", existingUser);
                return response;
            }
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .nickname(request.getNickname() != null ? request.getNickname() : request.getName())
                .phone(request.getPhone())
                .role("USER")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userMapper.insert(user);

        String token = jwtTokenProvider.generateToken(user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", user);

        return response;
    }

    /**
     * 로그인
     */
    public Map<String, Object> login(LoginRequest request) {
        User user = userMapper.findByEmail(request.getEmail());

        if (user == null) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new RuntimeException("이 계정은 Google 로그인으로만 접속 가능합니다");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        if (!user.getIsActive()) {
            throw new RuntimeException("비활성화된(탈퇴한) 계정입니다. 재가입을 통해 복구할 수 있습니다.");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", user);

        return response;
    }

    /**
     * 구글 로그인
     */
    @Transactional
    public Map<String, Object> googleLogin(String credential, String email, String name, String picture) {
        try {
            String userEmail = email;
            String userName = name;
            String userPicture = picture;

            if (credential != null && !credential.isEmpty()) {
                try {
                    try {
                        String decoded = new String(Base64.getDecoder().decode(credential));
                        JsonNode decodedJson = objectMapper.readTree(decoded);
                        if (decodedJson.has("email")) {
                            userEmail = decodedJson.get("email").asText();
                            userName = decodedJson.has("name") ? decodedJson.get("name").asText()
                                    : userEmail.split("@")[0];
                            userPicture = decodedJson.has("picture") ? decodedJson.get("picture").asText() : null;
                        }
                    } catch (Exception e) {
                        String[] parts = credential.split("\\.");
                        if (parts.length == 3) {
                            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                            JsonNode payloadJson = objectMapper.readTree(payload);

                            userEmail = payloadJson.get("email").asText();
                            userName = payloadJson.has("name") ? payloadJson.get("name").asText()
                                    : userEmail.split("@")[0];
                            userPicture = payloadJson.has("picture") ? payloadJson.get("picture").asText() : null;
                        }
                    }
                } catch (JsonProcessingException e) {
                    log.warn("Google credential 파싱 실패, 직접 전달된 정보 사용: {}", e.getMessage());
                }
            }

            if (email != null && !email.isEmpty()) {
                userEmail = email;
            }
            if (name != null && !name.isEmpty()) {
                userName = name;
            }
            if (picture != null && !picture.isEmpty()) {
                userPicture = picture;
            }

            if (userEmail == null || userEmail.isEmpty()) {
                throw new RuntimeException("Google 계정에서 이메일을 가져올 수 없습니다");
            }

            User user = userMapper.findByEmail(userEmail);

            if (user == null) {
                user = User.builder()
                        .email(userEmail)
                        .password("")
                        .name(userName)
                        .nickname(userName)
                        .profileImage(userPicture)
                        .role("USER")
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                userMapper.insert(user);
                log.info("Google 로그인으로 신규 사용자 생성: {}", userEmail);
                user = userMapper.findById(user.getUserId());
            } else {
                boolean isUpdated = false;
                // Soft Delete 된 계정 복구
                if (!user.getIsActive()) {
                    user.setIsActive(true);
                    log.info("Google 로그인으로 탈퇴 계정 복구: {}", userEmail);
                    isUpdated = true;
                }

                if (userPicture != null && !userPicture.equals(user.getProfileImage())) {
                    user.setProfileImage(userPicture);
                    isUpdated = true;
                }

                if (isUpdated) {
                    user.setUpdatedAt(LocalDateTime.now());
                    userMapper.update(user);
                    user = userMapper.findById(user.getUserId());
                }

                log.info("Google 로그인 성공: {}", userEmail);
            }

            String token = jwtTokenProvider.generateToken(user.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);

            return response;
        } catch (Exception e) {
            log.error("Google 로그인 오류: ", e);
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new RuntimeException("Google 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public String getGoogleClientId() {
        return googleClientId;
    }

    /**
     * 비밀번호 찾기 (임시 비밀번호 생성)
     */
    @Transactional
    public String forgotPassword(String email) {
        User user = userMapper.findByEmail(email);
        
        if (user == null) {
            throw new RuntimeException("해당 이메일로 등록된 계정이 없습니다");
        }
        
        if (!user.getIsActive()) {
            throw new RuntimeException("비활성화된(탈퇴한) 계정입니다");
        }
        
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new RuntimeException("이 계정은 Google 로그인으로만 접속 가능합니다");
        }
        
        // 임시 비밀번호 생성 (8자리 랜덤 문자열)
        String tempPassword = generateTempPassword();
        String encodedPassword = passwordEncoder.encode(tempPassword);
        
        // 비밀번호 업데이트
        user.setPassword(encodedPassword);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updatePassword(user);
        
        log.info("비밀번호 재설정 요청 - 이메일: {}", email);
        
        // 이메일로 임시 비밀번호 발송 (비동기 처리 - 즉시 반환)
        emailService.sendTempPasswordEmail(email, tempPassword);
        log.info("임시 비밀번호 이메일 발송 요청 완료 (비동기 처리 중): {}", email);
        
        // 보안을 위해 임시 비밀번호를 반환하지 않음
        return null;
    }
    
    /**
     * 임시 비밀번호 생성 (8자리 랜덤 문자열)
     */
    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
