package com.capstone.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private long refreshExpiresIn;

    @Builder.Default
    private String tokenType = "Bearer";

    @Builder.Default
    private long issuedAt = System.currentTimeMillis() / 1000;

}
