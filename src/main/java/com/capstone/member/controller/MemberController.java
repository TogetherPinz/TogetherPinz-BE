package com.capstone.member.controller;

import com.capstone.common.util.SecurityUtil;
import com.capstone.common.dto.ApiResponse;
import com.capstone.member.dto.*;
import com.capstone.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "멤버(Member)", description = "멤버 관리 API")
public class MemberController {

    private final MemberService memberService;
    private final SecurityUtil securityUtil;

    /**
     * POST /api/members/{pinId}
     * 핀에 새로운 멤버를 추가합니다.
     * 핀 소유자만 멤버를 추가할 수 있습니다.
     */
    @Operation(
        summary = "멤버 추가",
        description = "핀에 새로운 멤버를 추가합니다. 핀 소유자만 멤버를 추가할 수 있습니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "멤버 추가 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "핀을 찾을 수 없음")
    })
    @PostMapping("/{pinId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberInfo> addMember(
            @Parameter(description = "핀 ID", required = true, example = "1")
            @PathVariable Long pinId,
            @Parameter(description = "멤버 추가 요청 정보", required = true)
            @Valid @RequestBody AddMemberRequest request,
            HttpServletRequest httpRequest) {
        Long userId = securityUtil.getUserIdFromRequest(httpRequest);
        MemberInfo memberInfo = memberService.addMember(userId, pinId, request);
        return ApiResponse.success(memberInfo, "멤버가 추가되었습니다.");
    }

    /**
     * GET /api/members/{pinId}
     * 핀에 속한 모든 멤버를 조회합니다.
     * 내가 속한 핀의 멤버들만 조회 가능합니다.
     */
    @Operation(
        summary = "멤버 조회",
        description = "핀에 속한 모든 멤버를 조회합니다. 내가 속한 핀의 멤버들만 조회 가능합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "핀을 찾을 수 없음")
    })
    @GetMapping("/{pinId}")
    public ApiResponse<List<MemberInfo>> getMembers(
            @Parameter(description = "핀 ID", required = true, example = "1")
            @PathVariable Long pinId) {
        List<MemberInfo> members = memberService.getMembersByPinId(pinId);
        return ApiResponse.success(members);
    }

    /**
     * DELETE /api/members/{pinId}/{memberId}
     * 핀에서 멤버를 제거합니다.
     * 핀 소유자만 멤버를 제거할 수 있습니다.
     */
    @Operation(
        summary = "멤버 제거",
        description = "핀에서 멤버를 제거합니다. 핀 소유자만 멤버를 제거할 수 있습니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "멤버 제거 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "멤버를 찾을 수 없음")
    })
    @DeleteMapping("/{pinId}/{memberId}")
    public ApiResponse<Void> removeMember(
            @Parameter(description = "핀 ID", required = true, example = "1")
            @PathVariable Long pinId,
            @Parameter(description = "제거할 멤버 사용자 ID", required = true, example = "2")
            @PathVariable Long memberId,
            HttpServletRequest httpRequest) {
        Long userId = securityUtil.getUserIdFromRequest(httpRequest);
        memberService.removeMember(userId, pinId, memberId);
        return ApiResponse.success(null, "멤버가 제거되었습니다.");
    }

    /**
     * DELETE /api/members/{pinId}/leave
     * 핀에서 탈퇴합니다.
     * 내가 속한 핀만 탈퇴 가능합니다.
     * 내가 Owner일 경우 핀이 삭제됩니다.
     */
    @Operation(
        summary = "핀 탈퇴",
        description = "핀에서 탈퇴합니다. 내가 속한 핀만 탈퇴 가능합니다. 내가 Owner일 경우 핀이 삭제됩니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "탈퇴 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "핀을 찾을 수 없음")
    })
    @DeleteMapping("/{pinId}/leave")
    public ApiResponse<Void> leavePinGroup(
            @Parameter(description = "핀 ID", required = true, example = "1")
            @PathVariable Long pinId,
            HttpServletRequest httpRequest) {
        Long userId = securityUtil.getUserIdFromRequest(httpRequest);
        memberService.leavePinGroup(userId, pinId);
        return ApiResponse.success(null, "핀에서 탈퇴했습니다.");
    }
}
