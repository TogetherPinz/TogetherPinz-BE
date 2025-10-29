package com.capstone.auth.oauth2.user.userinfo;

import java.util.Map;

/** Kakao OAuth2 사용자 정보 구현체 */
public class KakaoOAuth2UserInfo implements OAuth2UserInfo {
    
    private final Map<String, Object> attributes;
    private final String id;
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> profile;
    
    @SuppressWarnings("unchecked")
    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.id = String.valueOf(attributes.get("id"));
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.profile = kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;
    }
    
    @Override
    public String getProvider() {
        return "kakao";
    }
    
    @Override
    public String getProviderId() {
        return id;
    }
    
    @Override
    public String getEmail() {
        return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
    }
    
    @Override
    public String getName() {
        return profile != null ? (String) profile.get("nickname") : null;
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
