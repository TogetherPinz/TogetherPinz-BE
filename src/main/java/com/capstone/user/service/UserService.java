package com.capstone.user.service;

import com.capstone.user.entity.User;
import com.capstone.user.dto.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserCacheService userCacheService;
    private final PasswordEncoder passwordEncoder;

    /** 사용자 생성 */
    @Transactional
    public UserInfo createUser(CreateUserRequest request) {
        // 중복 검사
        if (userCacheService.isExistsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자명입니다.");
        }
        if (request.getEmail() != null && userCacheService.isExistsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .build();

        User savedUser = userCacheService.saveUser(user);
        log.info("사용자 생성 성공: {}", savedUser.getUsername());

        return UserInfo.fromEntity(savedUser);
    }

    /** username으로 프로필 조회 */
    public UserInfo getUserProfile(String username) {
        User user = userCacheService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return UserInfo.fromEntity(user);
    }

    /** 프로필 수정 */
    @Transactional
    public UserInfo updateUserProfile(String username, UpdateUserRequest request) {
        User user = userCacheService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이메일 중붙 검사
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userCacheService.isExistsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
        }

        user.updateProfile(request.getName(), request.getPhone(), request.getEmail());
        User updatedUser = userCacheService.updateUser(user);
        log.info("프로필 수정 성공: {}", updatedUser.getUsername());

        return UserInfo.fromEntity(updatedUser);
    }

    /** 프로필 삭제 */
    @Transactional
    public void deleteUserProfile(String username) {
        User user = userCacheService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        userCacheService.deleteUser(user.getId());
        log.info("사용자 삭제 성공: userId={}", user.getId());
    }

    /** 아이디 찾기 (전화번호 또는 이메일) */
    public FindUsernameResponse findUsername(FindUsernameRequest request) {
        User user = null;

        if (request.getPhone() != null) {
            user = userCacheService.getUserByPhone(request.getPhone()).orElse(null);
        }
        
        if (user == null && request.getEmail() != null) {
            user = userCacheService.getUserByEmail(request.getEmail()).orElse(null);
        }

        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        return FindUsernameResponse.builder()
                .username(user.getUsername())
                .build();
    }

    /** 비밀번호 찾기 (재설정) */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userCacheService.getUserByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이메일 확인
        if (!user.getEmail().equals(request.getEmail())) {
            throw new IllegalArgumentException("이메일이 일치하지 않습니다.");
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userCacheService.updateUser(user);
        log.info("비밀번호 재설정 성공: {}", user.getUsername());
    }
}
