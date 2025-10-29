package com.capstone.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.capstone.user.dto.UserInfo;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private String accessToken;

    private String refreshToken;

    private UserInfo userInfo;

    private long expiresIn;

    private long refreshExpiresIn;

    @Builder.Default
    private String tokenType = "Bearer";

    @Builder.Default
    private long issuedAt = System.currentTimeMillis() / 1000;

}
