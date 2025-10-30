package com.capstone.user.controller;

import com.capstone.common.dto.ApiResponse;
import com.capstone.user.dto.*;
import com.capstone.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "사용자(User)", description = "사용자 프로필 관리 API")
public class UserController {

    private final UserService userService;

    @Operation(
        summary = "사용자 생성",
        description = "새로운 사용자를 생성합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "사용자 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<UserInfo>> createUser(
            @Parameter(description = "사용자 생성 요청 정보", required = true)
            @RequestBody CreateUserRequest request) {
        UserInfo userInfo = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(userInfo, "사용자 생성이 성공했습니다."));
    }

    @Operation(
        summary = "프로필 조회",
        description = "사용자 ID로 프로필 정보를 조회합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserInfo>> getUserProfile(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable Long id) {
        UserInfo userInfo = userService.getUserProfile(id);
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    @Operation(
        summary = "프로필 수정",
        description = "사용자 프로필 정보를 수정합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ApiResponse<UserInfo> updateUserProfile(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "프로필 수정 요청 정보", required = true)
            @RequestBody UpdateUserRequest request) {
        UserInfo userInfo = userService.updateUserProfile(id, request);
        return ApiResponse.success(userInfo, "프로필 수정이 성공했습니다.");
    }

    @Operation(
        summary = "프로필 삭제",
        description = "사용자 프로필을 삭제합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUserProfile(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable Long id) {
        userService.deleteUserProfile(id);
        return ApiResponse.success(null, "프로필 삭제가 성공했습니다.");
    }

    @Operation(
        summary = "아이디 찾기",
        description = "전화번호 또는 이메일로 사용자 아이디를 찾습니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "아이디 찾기 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping("/username")
    public ApiResponse<FindUsernameResponse> findUsername(
            @Parameter(description = "아이디 찾기 요청 정보 (전화번호 또는 이메일)", required = true)
            @RequestBody FindUsernameRequest request) {
        FindUsernameResponse response = userService.findUsername(request);
        return ApiResponse.success(response);
    }

    @Operation(
        summary = "비밀번호 재설정",
        description = "사용자 아이디, 이메일 확인 후 비밀번호를 재설정합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/password")
    public ApiResponse<Void> resetPassword(
            @Parameter(description = "비밀번호 재설정 요청 정보", required = true)
            @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ApiResponse.success(null, "비밀번호가 재설정되었습니다.");
    }

}
