package com.capstone.auth.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyTokenResponse {

    private boolean valid;
    private String username;
    private Long userId;
    private String message;

}
