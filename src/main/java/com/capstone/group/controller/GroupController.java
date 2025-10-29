package com.capstone.group.controller;

import com.capstone.common.dto.ApiResponse;
import com.capstone.group.dto.*;
import com.capstone.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
@Tag(name = "그룹(Group)", description = "그룹 관리 API")
public class GroupController {

    private final GroupService groupService;

    @Operation(
        summary = "그룹 생성",
        description = "핀을 기반으로 새로운 그룹을 생성합니다. 핀 소유자만 그룹을 생성할 수 있습니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "그룹 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GroupInfo> createGroup(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestParam Long userId,
            @Parameter(description = "그룹 생성 요청 정보", required = true)
            @Valid @RequestBody CreateGroupRequest request) {
        GroupInfo groupInfo = groupService.createGroup(userId, request);
        return ApiResponse.success(groupInfo, "그룹이 생성되었습니다.");
    }

    @Operation(
        summary = "그룹 목록 조회",
        description = "사용자가 속한 모든 그룹 또는 특정 핀에 속한 모든 그룹을 조회합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ApiResponse<List<GroupInfo>> getGroups(
            @Parameter(description = "사용자 ID (선택사항)", example = "1")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "핀 ID (선택사항)", example = "1")
            @RequestParam(required = false) Long pinId) {
        List<GroupInfo> groups;
        
        if (userId != null) {
            groups = groupService.getGroupsByUserId(userId);
        } else if (pinId != null) {
            groups = groupService.getGroupsByPinId(pinId);
        } else {
            throw new IllegalArgumentException("사용자 ID 또는 핀 ID를 지정해야 합니다.");
        }
        
        return ApiResponse.success(groups);
    }

    @Operation(
        summary = "그룹 단건 조회",
        description = "특정 그룹의 상세 정보를 조회합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ApiResponse<GroupInfo> getGroup(
            @Parameter(description = "그룹 ID", required = true, example = "1")
            @PathVariable Long id) {
        GroupInfo groupInfo = groupService.getGroup(id);
        return ApiResponse.success(groupInfo);
    }

    @Operation(
        summary = "그룹 수정",
        description = "그룹의 핀을 변경합니다. 그룹 소유자만 수정할 수 있습니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ApiResponse<GroupInfo> updateGroup(
            @Parameter(description = "그룹 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestParam Long userId,
            @Parameter(description = "그룹 수정 요청 정보", required = true)
            @Valid @RequestBody UpdateGroupRequest request) {
        GroupInfo groupInfo = groupService.updateGroup(userId, id, request);
        return ApiResponse.success(groupInfo, "그룹이 수정되었습니다.");
    }

    @Operation(
        summary = "그룹 삭제 (탈퇴)",
        description = "사용자가 그룹에서 탈퇴합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteGroup(
            @Parameter(description = "그룹 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestParam Long userId) {
        groupService.deleteGroup(userId, id);
        return ApiResponse.success(null, "그룹에서 탈퇴했습니다.");
    }

    @Operation(
        summary = "그룹 멤버 추가",
        description = "핀에 새로운 멤버를 추가합니다. 핀 소유자만 멤버를 추가할 수 있습니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "멤버 추가 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/{id}/member")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GroupInfo> addMember(
            @Parameter(description = "핀 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "요청 사용자 ID", required = true, example = "1")
            @RequestParam Long userId,
            @Parameter(description = "멤버 추가 요청 정보", required = true)
            @Valid @RequestBody AddMemberRequest request) {
        GroupInfo groupInfo = groupService.addMember(userId, id, request);
        return ApiResponse.success(groupInfo, "그룹 멤버가 추가되었습니다.");
    }

    @Operation(
        summary = "그룹 멤버 제거",
        description = "핀에서 멤버를 제거합니다. 핀 소유자만 멤버를 제거할 수 있습니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "멤버 제거 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "멤버를 찾을 수 없음")
    })
    @DeleteMapping("/{id}/member/{memberId}")
    public ApiResponse<Void> removeMember(
            @Parameter(description = "핀 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "제거할 멤버 ID", required = true, example = "2")
            @PathVariable Long memberId,
            @Parameter(description = "요청 사용자 ID", required = true, example = "1")
            @RequestParam Long userId) {
        groupService.removeMember(userId, id, memberId);
        return ApiResponse.success(null, "그룹 멤버가 제거되었습니다.");
    }
}
