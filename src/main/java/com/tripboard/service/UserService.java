package com.tripboard.service;

import com.tripboard.entity.User;
import com.tripboard.entity.UserTravelPreference;
import com.tripboard.mapper.UserMapper;
import com.tripboard.mapper.UserTravelPreferenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserTravelPreferenceMapper preferenceMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 정보 조회
     */
    public User getUserInfo(Long userId) {
        return userMapper.findById(userId);
    }

    /**
     * 사용자 여행 성향 조회
     */
    public UserTravelPreference getUserPreference(Long userId) {
        return preferenceMapper.findByUserId(userId);
    }

    /**
     * 사용자 여행 성향 저장 또는 업데이트
     */
    @Transactional
    public UserTravelPreference saveOrUpdatePreference(Long userId, UserTravelPreference preference) {
        UserTravelPreference existing = preferenceMapper.findByUserId(userId);

        if (existing != null) {
            preference.setPreferenceId(existing.getPreferenceId());
            preference.setUserId(userId);
            preference.setCreatedAt(existing.getCreatedAt());
            preference.setUpdatedAt(LocalDateTime.now());
            preferenceMapper.update(preference);
            return preference;
        } else {
            preference.setUserId(userId);
            preference.setCreatedAt(LocalDateTime.now());
            preference.setUpdatedAt(LocalDateTime.now());
            preferenceMapper.insert(preference);
            return preference;
        }
    }

    /**
     * 닉네임 변경
     */
    @Transactional
    public User updateNickname(Long userId, String nickname) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다");
        }

        user.setNickname(nickname);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
        return user;
    }

    /**
     * 프로필 이미지 변경
     */
    @Transactional
    public User updateProfileImage(Long userId, String profileImage) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다");
        }

        user.setProfileImage(profileImage);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
        return user;
    }

    /**
     * 자기소개(Bio) 변경
     */
    @Transactional
    public User updateBio(Long userId, String bio) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다");
        }

        user.setBio(bio);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
        return user;
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다");
        }

        // Google 로그인 사용자는 비밀번호가 없을 수 있음
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new RuntimeException("이 계정은 Google 로그인으로만 접속 가능합니다. 비밀번호를 변경할 수 없습니다.");
        }

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다");
        }

        // 새 비밀번호 암호화 및 저장
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updatePassword(user);
    }

    /**
     * 회원탈퇴 (소프트 삭제: is_active를 false로 설정)
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다");
        }

        userMapper.delete(userId);
    }
}
