package com.capstone.user.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class ResetPasswordRequest {

    private String username;
    private String email;
    private String newPassword;

}
