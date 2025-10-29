package com.capstone.auth.oauth2.service;

import com.capstone.auth.oauth2.user.CustomOAuth2User;
import com.capstone.auth.oauth2.user.userinfo.OAuth2UserInfo;
import com.capstone.auth.oauth2.user.userinfo.OAuth2UserInfoFactory;
import com.capstone.user.entity.User;
import com.capstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/** OAuth2 로그인 시 사용자 정보를 처리하는 서비스 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // OAuth2 제공자로부터 사용자 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // OAuth2 제공자 ID (kakao, naver, google)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        // OAuth2UserInfo 추상화
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
            registrationId, 
            oAuth2User.getAttributes()
        );
        
        // 사용자 정보 확인
        if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
            throw new OAuth2AuthenticationException("이메일 정보를 찾을 수 없습니다.");
        }
        
        // 기존 사용자 조회 또는 신규 사용자 생성
        User user = getOrCreateUser(oAuth2UserInfo);
        
        log.info("OAuth2 로그인 성공: provider={}, email={}", 
                oAuth2UserInfo.getProvider(), oAuth2UserInfo.getEmail());
        
        // CustomOAuth2User 반환
        return new CustomOAuth2User(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getName(),
            user.getProvider(),
            user.getProviderId(),
            oAuth2User.getAttributes()
        );
    }
    
    /** 기존 사용자 조회 또는 신규 사용자 생성 */
    private User getOrCreateUser(OAuth2UserInfo oAuth2UserInfo) {
        // 1. provider + providerId로 기존 사용자 조회
        Optional<User> userOptional = userRepository.findByProviderAndProviderId(
            oAuth2UserInfo.getProvider(),
            oAuth2UserInfo.getProviderId()
        );
        
        if (userOptional.isPresent()) {
            return userOptional.get();
        }
        
        // 2. 이메일로 기존 사용자 조회 (소셜 계정 연동)
        Optional<User> emailUserOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        
        if (emailUserOptional.isPresent()) {
            User existingUser = emailUserOptional.get();
            // 이미 다른 provider로 연동된 경우
            if (existingUser.getProvider() != null) {
                throw new IllegalArgumentException(
                    "이미 " + existingUser.getProvider() + " 계정으로 연동되어 있습니다."
                );
            }
            // provider 정보 업데이트는 하지 않고 새로운 사용자로 생성
        }
        
        // 3. 신규 사용자 생성
        String username = generateUsername(oAuth2UserInfo);
        
        User newUser = User.builder()
            .username(username)
            .password(UUID.randomUUID().toString()) // OAuth2 사용자는 비밀번호 사용 안함
            .name(oAuth2UserInfo.getName() != null ? oAuth2UserInfo.getName() : "사용자")
            .email(oAuth2UserInfo.getEmail())
            .provider(oAuth2UserInfo.getProvider())
            .providerId(oAuth2UserInfo.getProviderId())
            .build();
        
        return userRepository.save(newUser);
    }
    
    /** OAuth2 사용자를 위한 고유 username 생성 */
    private String generateUsername(OAuth2UserInfo oAuth2UserInfo) {
        String baseUsername = oAuth2UserInfo.getProvider() + "_" + oAuth2UserInfo.getProviderId();
        
        // username 중복 체크
        String username = baseUsername;
        int suffix = 1;
        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + "_" + suffix++;
        }
        
        return username;
    }
}
