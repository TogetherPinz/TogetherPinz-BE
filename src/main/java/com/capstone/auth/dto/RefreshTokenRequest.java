package com.capstone.auth.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {

    @NotBlank(message = "리프레시 토큰은 필수입니다")
    private String refreshToken;

}
