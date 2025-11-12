package com.capstone.auth.service;

import com.capstone.auth.dto.*;
import com.capstone.common.util.JwtUtil;
import com.capstone.user.dto.CreateUserRequest;
import com.capstone.user.dto.UserInfo;
import com.capstone.user.entity.User;
import com.capstone.user.repository.UserRepository;
import com.capstone.user.service.UserCacheService;
import com.capstone.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserService userService;
    private final UserCacheService userCacheService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    /** 사용자 등록 */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // 사용자 생성
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .build();

        UserInfo userInfo = userService.createUser(createUserRequest);
        log.info("회원가입 성공: {}", userInfo.getUsername());

        return RegisterResponse.builder()
                .username(userInfo.getUsername())
                .message("회원가입이 완료되었습니다.")
                .build();
    }

    /** 사용자 로그인 */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userCacheService.getUserByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());

        // Refresh Token Redis에 저장
        String redisKey = REFRESH_TOKEN_PREFIX + user.getUsername();
        redisTemplate.opsForValue().set(
                redisKey,
                refreshToken,
                jwtUtil.getRefreshTokenExpirationInSeconds(),
                TimeUnit.SECONDS
        );

        log.info("로그인 성공: {}", user.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userInfo(UserInfo.fromEntity(user))
                .expiresIn(jwtUtil.getAccessTokenExpirationInSeconds())
                .refreshExpiresIn(jwtUtil.getRefreshTokenExpirationInSeconds())
                .build();
    }

    /** 사용자 로그아웃 */
    @Transactional
    public void logout(String token, String username) {
        // Refresh Token 삭제
        String refreshKey = REFRESH_TOKEN_PREFIX + username;
        redisTemplate.delete(refreshKey);

        // Access Token 블랙리스트 추가
        String blacklistKey = BLACKLIST_PREFIX + token;
        long expiration = jwtUtil.getExpirationFromToken(token).getTime() - System.currentTimeMillis();
        if (expiration > 0) {
            redisTemplate.opsForValue().set(
                    blacklistKey,
                    "true",
                    expiration,
                    TimeUnit.MILLISECONDS
            );
        }

        log.info("로그아웃 성공: {}", username);
    }

    /** 토큰 갱신 */
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Refresh Token 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 토큰 타입 확인
        if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
            throw new IllegalArgumentException("리프레시 토큰이 아닙니다.");
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        // Redis에 저장된 Refresh Token 확인
        String redisKey = REFRESH_TOKEN_PREFIX + username;
        String storedToken = redisTemplate.opsForValue().get(redisKey);

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 새로운 토큰 생성
        String newAccessToken = jwtUtil.generateAccessToken(username, userId);
        String newRefreshToken = jwtUtil.generateRefreshToken(username, userId);

        // 새로운 Refresh Token Redis에 저장
        redisTemplate.opsForValue().set(
                redisKey,
                newRefreshToken,
                jwtUtil.getRefreshTokenExpirationInSeconds(),
                TimeUnit.SECONDS
        );

        log.info("토큰 갱신 성공: {}", username);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtUtil.getAccessTokenExpirationInSeconds())
                .refreshExpiresIn(jwtUtil.getRefreshTokenExpirationInSeconds())
                .build();
    }

    /** 비밀번호 재설정 */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // UserService의 resetPassword 사용
        com.capstone.user.dto.ResetPasswordRequest userResetRequest = 
                com.capstone.user.dto.ResetPasswordRequest.builder()
                        .username(request.getUsername())
                        .email(request.getEmail())
                        .newPassword(request.getNewPassword())
                        .build();

        userService.resetPassword(userResetRequest);
        log.info("비밀번호 재설정 성공: {}", request.getUsername());
    }

    /** 토큰 검증 */
    public VerifyTokenResponse verifyToken(String token) {
        try {
            // 블랙리스트 확인
            String blacklistKey = BLACKLIST_PREFIX + token;
            if (redisTemplate.hasKey(blacklistKey)) {
                return VerifyTokenResponse.builder()
                        .valid(false)
                        .message("로그아웃된 토큰입니다.")
                        .build();
            }

            // 토큰 검증
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);

                return VerifyTokenResponse.builder()
                        .valid(true)
                        .username(username)
                        .userId(userId)
                        .message("유효한 토큰입니다.")
                        .build();
            } else {
                return VerifyTokenResponse.builder()
                        .valid(false)
                        .message("유효하지 않은 토큰입니다.")
                        .build();
            }
        } catch (Exception e) {
            log.error("토큰 검증 중 오류 발생: {}", e.getMessage());
            return VerifyTokenResponse.builder()
                    .valid(false)
                    .message("토큰 검증 실패: " + e.getMessage())
                    .build();
        }
    }

    /** OAuth2 로그인 (Google ID Token 검증) */
    @Transactional
    public LoginResponse oauth2Login(String idToken) {
        try {
            // Google ID Token 검증
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), 
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new IllegalArgumentException("유효하지 않은 Google ID Token입니다.");
            }

            // 사용자 정보 추출
            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String providerId = payload.getSubject();

            log.info("Google ID Token 검증 성공: email={}, name={}", email, name);

            // 기존 사용자 조회 또는 신규 사용자 생성
            User user = getOrCreateOAuth2User(email, name, providerId);

            // JWT 토큰 생성
            String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());

            // Refresh Token Redis에 저장
            String redisKey = REFRESH_TOKEN_PREFIX + user.getUsername();
            redisTemplate.opsForValue().set(
                    redisKey,
                    refreshToken,
                    jwtUtil.getRefreshTokenExpirationInSeconds(),
                    TimeUnit.SECONDS
            );

            log.info("OAuth2 로그인 성공: {}", user.getUsername());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userInfo(UserInfo.fromEntity(user))
                    .expiresIn(jwtUtil.getAccessTokenExpirationInSeconds())
                    .refreshExpiresIn(jwtUtil.getRefreshTokenExpirationInSeconds())
                    .build();

        } catch (GeneralSecurityException | IOException e) {
            log.error("Google ID Token 검증 실패: {}", e.getMessage());
            throw new IllegalArgumentException("Google ID Token 검증에 실패했습니다: " + e.getMessage());
        }
    }

    /** OAuth2 사용자 조회 또는 생성 */
    private User getOrCreateOAuth2User(String email, String name, String providerId) {
        // 1. provider + providerId로 기존 사용자 조회
        Optional<User> userOptional = userRepository.findByProviderAndProviderId("google", providerId);
        
        if (userOptional.isPresent()) {
            return userOptional.get();
        }
        
        // 2. 이메일로 기존 사용자 조회
        Optional<User> emailUserOptional = userRepository.findByEmail(email);
        
        if (emailUserOptional.isPresent()) {
            User existingUser = emailUserOptional.get();
            // 이미 다른 provider로 연동된 경우
            if (existingUser.getProvider() != null && !"google".equals(existingUser.getProvider())) {
                throw new IllegalArgumentException(
                    "이미 " + existingUser.getProvider() + " 계정으로 연동되어 있습니다."
                );
            }
        }
        
        // 3. 신규 사용자 생성
        String username = generateOAuth2Username("google", providerId);
        
        User newUser = User.builder()
                .username(username)
                .password(UUID.randomUUID().toString()) // OAuth2 사용자는 비밀번호 사용 안함
                .name(name != null ? name : "사용자")
                .email(email)
                .provider("google")
                .providerId(providerId)
                .build();
        
        return userRepository.save(newUser);
    }

    /** OAuth2 사용자를 위한 고유 username 생성 */
    private String generateOAuth2Username(String provider, String providerId) {
        String baseUsername = provider + "_" + providerId;
        
        // username 중복 체크
        String username = baseUsername;
        int suffix = 1;
        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + "_" + suffix++;
        }
        
        return username;
    }

}
