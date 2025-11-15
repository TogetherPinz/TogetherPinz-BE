package com.capstone.auth.controller;

import com.capstone.auth.dto.*;
import com.capstone.user.dto.FindUsernameRequest;
import com.capstone.user.dto.FindUsernameResponse;
import com.capstone.auth.service.AuthService;
import com.capstone.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증(Authentication)", description = "사용자 인증 및 계정 관리 API")
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "회원가입",
        description = "새로운 사용자를 등록합니다. 아이디, 비밀번호, 이름, 연락처, 이메일이 필요합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 필드 누락, 중복된 아이디 등)")
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponse> register(
            @Parameter(description = "회원가입 요청 정보", required = true)
            @Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ApiResponse.success(response, "회원가입이 완료되었습니다.");
    }

    @Operation(
        summary = "로그인",
        description = "아이디와 비밀번호로 로그인하여 JWT 토큰을 발급받습니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공, JWT 토큰 반환"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (아이디 또는 비밀번호 불일치)")
    })
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<LoginResponse> login(
            @Parameter(description = "로그인 요청 정보", required = true)
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response, "로그인이 성공했습니다.");
    }

    @Operation(
        summary = "로그아웃",
        description = "현재 액세스 토큰을 무효화하고 리프레시 토큰을 삭제합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @Parameter(description = "JWT 액세스 토큰 (Bearer {token})", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "사용자 아이디", required = true, example = "user123")
            @RequestParam String username) {
        // Bearer 토큰 추출
        String token = authorization.substring(7);
        authService.logout(token, username);
        return ApiResponse.success(null, "로그아웃이 완료되었습니다.");
    }

    @Operation(
        summary = "토큰 갱신",
        description = "리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급받습니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
    })
    @PostMapping("/token")
    public ApiResponse<TokenResponse> refreshToken(
            @Parameter(description = "리프레시 토큰 요청 정보", required = true)
            @Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refreshToken(request);
        return ApiResponse.success(response, "토큰이 갱신되었습니다.");
    }

    @Operation(
        summary = "아이디 찾기",
        description = "이름과 이메일을 통해 사용자의 아이디를 찾습니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "아이디 찾기 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 정보 불일치")
    })
    @PostMapping("/username")
    public ApiResponse<FindUsernameResponse> findUsername(FindUsernameRequest request) {
        FindUsernameResponse response = authService.findUsername(request);
        return ApiResponse.success(response, "아이디 찾기가 완료되었습니다.");
    }

    @Operation(
        summary = "비밀번호 재설정",
        description = "사용자 아이디와 이메일을 확인하여 새로운 비밀번호로 변경합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (아이디 또는 이메일 불일치)")
    })
    @PostMapping("/password")
    public ApiResponse<Void> resetPassword(
            @Parameter(description = "비밀번호 재설정 요청 정보", required = true)
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success(null, "비밀번호가 재설정되었습니다.");
    }

    @Operation(
        summary = "토큰 검증",
        description = "JWT 액세스 토큰의 유효성을 검증하고 사용자 정보를 반환합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 유효"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 무효 또는 만료")
    })
    @PostMapping("/verify")
    public ApiResponse<VerifyTokenResponse> verifyToken(
            @Parameter(description = "JWT 액세스 토큰 (Bearer {token})", required = true)
            @RequestHeader("Authorization") String authorization) {
        // Bearer 토큰 추출
        String token = authorization.substring(7);
        VerifyTokenResponse response = authService.verifyToken(token);
        return ApiResponse.success(response);
    }

    @Operation(
        summary = "OAuth2 로그인 (Google)",
        description = "Android에서 구글 로그인을 통해 생성한 ID Token을 검증하여 사용자를 인증합니다. " +
                      "ID Token이 유효하면 새 사용자를 생성하거나 기존 사용자로 로그인 처리하고, JWT 액세스 토큰과 리프레시 토큰을 반환합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OAuth2 로그인 성공, JWT 토큰 반환"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 ID Token")
    })
    @PostMapping("/oauth2/google")
    public ApiResponse<LoginResponse> oauth2GoogleLogin(
            @Parameter(description = "Google ID Token 요청 정보", required = true)
            @Valid @RequestBody OAuth2LoginRequest request) {
        LoginResponse response = authService.oauth2Login(request.getIdToken());
        return ApiResponse.success(response, "OAuth2 로그인이 성공했습니다.");
    }

}
