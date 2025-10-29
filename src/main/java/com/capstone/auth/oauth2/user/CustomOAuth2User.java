package com.capstone.auth.oauth2.user;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/** OAuth2 인증 사용자 정보를 담는 커스텀 클래스 */
@Getter
public class CustomOAuth2User implements OAuth2User {
    
    private final Long userId;
    private final String username;
    private final String email;
    private final String name;
    private final String provider;
    private final String providerId;
    private final Map<String, Object> attributes;
    
    public CustomOAuth2User(Long userId, String username, String email, String name, 
                           String provider, String providerId, Map<String, Object> attributes) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.name = name;
        this.provider = provider;
        this.providerId = providerId;
        this.attributes = attributes;
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }
    
    @Override
    public String getName() {
        return username;
    }
}
