package com.capstone.auth.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "LoginRequest", description = "로그인 요청 DTO")
public class LoginRequest {

    @Schema(description = "아이디", example = "user123")
    @NotBlank(message = "아이디는 필수입니다")
    @Size(min = 4, max =100, message = "아이디는 4자 이상 100자 이하여야 합니다")
    private String username; // 아이디

    @Schema(description = "비밀번호", example = "securePassword!")
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 4, max =255, message = "비밀번호는 4자 이상 255자 이하여야 합니다")
    private String password; // 비밀번호

}
