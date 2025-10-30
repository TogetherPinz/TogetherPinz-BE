package com.capstone.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth2 로그인 요청 (Google ID Token)")
public class OAuth2LoginRequest {
    
    @NotBlank(message = "ID Token은 필수입니다.")
    @Schema(description = "Google OAuth2 ID Token", 
            example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE4MmU0NTBhMzVhMjA4MWZhYTFkOWViYTIy...",
            required = true)
    private String idToken;
}
