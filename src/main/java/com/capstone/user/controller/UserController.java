package com.capstone.user.controller;

import com.capstone.common.util.SecurityUtil;
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
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "사용자(User)", description = "사용자 프로필 관리 API")
public class UserController {

    private final UserService userService;
    private final SecurityUtil securityUtil;

    @Operation(
        summary = "프로필 조회",
        description = "username으로 프로필 정보를 조회합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserInfo>> getUserProfile(
            @Parameter(description = "사용자 username", required = true, example = "test")
            @PathVariable String username) {
        UserInfo userInfo = userService.getUserProfile(username);
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
    @PutMapping("/{username}")
    public ApiResponse<UserInfo> updateUserProfile(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable String username,
            @Parameter(description = "프로필 수정 요청 정보", required = true)
            @RequestBody UpdateUserRequest request,
            HttpServletRequest httpRequest) {
        if (!securityUtil.getUsernameFromRequest(httpRequest).equals(username)) {
            throw new IllegalArgumentException("자신의 프로필만 수정할 수 있습니다.");
        }
        UserInfo userInfo = userService.updateUserProfile(username, request);
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
    @DeleteMapping("/{username}")
    public ApiResponse<Void> deleteUserProfile(
            @Parameter(description = "사용자 username", required = true, example = "test")
            @PathVariable String username,
            HttpServletRequest httpRequest) {
        if (!securityUtil.getUsernameFromRequest(httpRequest).equals(username)) {
            throw new IllegalArgumentException("자신의 프로필만 삭제할 수 있습니다.");
        }
        userService.deleteUserProfile(username);
        return ApiResponse.success(null, "프로필 삭제가 성공했습니다.");
    }

}
