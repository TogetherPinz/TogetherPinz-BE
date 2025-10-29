package com.capstone.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "RegisterRequest", description = "회원가입 요청 DTO")
public class RegisterRequest {

    @Schema(description = "아이디", example = "user123")
    @NotBlank(message = "아이디는 필수입니다")
    @Size(min = 4, max =100, message = "아이디는 4자 이상 100자 이하여야 합니다")
    private String username;

    @Schema(description = "비밀번호", example = "securePassword!")
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 4, max =255, message = "비밀번호는 4자 이상 255자 이하여야 합니다")
    private String password;

    @Schema(description = "비밀번호 확인", example = "securePassword!")
    @NotBlank(message = "비밀번호 확인은 필수입니다")
    private String confirmPassword;

    @Schema(description = "이름", example = "홍길동")
    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 30, message = "이름은 2자 이상 30자 이하여야 합니다")
    private String name;

    @Schema(description = "이메일", example = "togetherpins@pins.com")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Schema(description = "전화번호", example = "010-1234-5678")
    @Pattern(regexp = "^[0-9-+()\\s]*$", message = "올바른 전화번호 형식이 아닙니다")
    private String phone;

}
