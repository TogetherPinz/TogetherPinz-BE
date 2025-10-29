package com.capstone.auth.oauth2.user.userinfo;

import java.util.Map;

/** Naver OAuth2 사용자 정보 구현체 */
public class NaverOAuth2UserInfo implements OAuth2UserInfo {
    
    private final Map<String, Object> attributes;
    private final Map<String, Object> response;
    
    @SuppressWarnings("unchecked")
    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.response = (Map<String, Object>) attributes.get("response");
    }
    
    @Override
    public String getProvider() {
        return "naver";
    }
    
    @Override
    public String getProviderId() {
        return response != null ? (String) response.get("id") : null;
    }
    
    @Override
    public String getEmail() {
        return response != null ? (String) response.get("email") : null;
    }
    
    @Override
    public String getName() {
        return response != null ? (String) response.get("name") : null;
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
