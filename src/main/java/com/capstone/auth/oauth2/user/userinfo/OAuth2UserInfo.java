package com.capstone.auth.oauth2.user.userinfo;

import java.util.Map;

/** OAuth2 제공자로부터 받은 사용자 정보를 추상화하는 인터페이스 */
public interface OAuth2UserInfo {
    
    /** OAuth2 제공자 ID (kakao, naver, google) */
    String getProvider();
    
    /** OAuth2 제공자의 사용자 고유 ID */
    String getProviderId();
    
    /** 사용자 이메일 */
    String getEmail();
    
    /** 사용자 이름 */
    String getName();
    
    /** 원본 attributes */
    Map<String, Object> getAttributes();
}
