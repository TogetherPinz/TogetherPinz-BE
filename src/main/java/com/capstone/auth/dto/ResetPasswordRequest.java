package com.capstone.auth.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {

    @NotBlank(message = "아이디는 필수입니다")
    private String username;

    @NotBlank(message = "이메일은 필수입니다")
    private String email;

    @NotBlank(message = "새 비밀번호는 필수입니다")
    private String newPassword;

}
