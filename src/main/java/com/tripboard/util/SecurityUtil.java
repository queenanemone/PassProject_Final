package com.tripboard.util;

import com.tripboard.entity.User;
import com.tripboard.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {
    
    private final UserMapper userMapper;
    
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            // anonymousUser 체크
            if (email != null && !email.equals("anonymousUser") && !email.isEmpty()) {
                return email;
            }
        }
        return null;
    }
    
    public Long getCurrentUserId() {
        try {
            String email = getCurrentUserEmail();
            if (email != null && !email.isEmpty() && !email.equals("anonymousUser")) {
                User user = userMapper.findByEmail(email);
                if (user != null) {
                    return user.getUserId();
                } else {
                    System.err.println("사용자를 찾을 수 없습니다: " + email);
                }
            } else {
                System.err.println("이메일이 null이거나 비어있습니다: " + email);
            }
        } catch (Exception e) {
            System.err.println("getCurrentUserId 오류: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}

