package com.capstone.auth.oauth2.handler;

import com.capstone.auth.oauth2.user.CustomOAuth2User;
import com.capstone.common.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/** OAuth2 로그인 성공 시 JWT 토큰을 발급하고 리다이렉트 처리하는 핸들러 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;
    
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        
        // JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(oAuth2User.getUsername(), oAuth2User.getUserId());
        String refreshToken = jwtUtil.generateRefreshToken(oAuth2User.getUsername(), oAuth2User.getUserId());
        
        // Refresh Token을 Redis에 저장
        String redisKey = REFRESH_TOKEN_PREFIX + oAuth2User.getUsername();
        redisTemplate.opsForValue().set(
            redisKey,
            refreshToken,
            jwtUtil.getRefreshTokenExpirationInSeconds(),
            TimeUnit.SECONDS
        );
        
        log.info("OAuth2 로그인 성공 - 토큰 발급: username={}, provider={}", 
                oAuth2User.getUsername(), oAuth2User.getProvider());
        
        // 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("accessToken", accessToken)
            .queryParam("refreshToken", refreshToken)
            .queryParam("userId", oAuth2User.getUserId())
            .queryParam("username", oAuth2User.getUsername())
            .build()
            .toUriString();
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
