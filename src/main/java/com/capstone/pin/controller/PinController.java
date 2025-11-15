package com.capstone.pin.controller;

import com.capstone.common.dto.ApiResponse;
import com.capstone.common.util.SecurityUtil;
import com.capstone.pin.dto.*;
import com.capstone.pin.service.PinService;
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
@RequestMapping("/api/pins")
@RequiredArgsConstructor
@Tag(name = "핀(Pin)", description = "핀 관리 API")
public class PinController {

    private final PinService pinService;
    private final SecurityUtil securityUtil;

    @Operation(
        summary = "핀 생성",
        description = "새로운 핀을 생성합니다. 핀은 위치 기반 태그로, 할 일을 분류하는 데 사용됩니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "핀 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PinInfo> createPin(
            @Parameter(description = "핀 생성 요청 정보", required = true)
            @Valid @RequestBody CreatePinRequest request,
            HttpServletRequest httpRequest) {
        Long userId = securityUtil.getUserIdFromRequest(httpRequest);
        PinInfo pinInfo = pinService.createPin(userId, request);
        return ApiResponse.success(pinInfo, "핀이 생성되었습니다.");
    }

    @Operation(
        summary = "핀 목록 조회",
        description = "전체 핀 목록 또는 특정 사용자의 핀 목록을 조회합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ApiResponse<List<PinInfo>> getPins(
            HttpServletRequest httpRequest) {
        Long userId = securityUtil.getUserIdFromRequest(httpRequest);
        List<PinInfo> pins = pinService.getUserPins(userId);
        return ApiResponse.success(pins);
    }

    @Operation(
        summary = "핀 단건 조회",
        description = "특정 핀의 상세 정보를 조회합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "핀을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ApiResponse<PinInfo> getPin(
            @Parameter(description = "핀 ID", required = true, example = "1")
            @PathVariable Long id) {
        PinInfo pinInfo = pinService.getPin(id);
        return ApiResponse.success(pinInfo);
    }

    @Operation(
        summary = "핀 수정",
        description = "핀의 정보를 수정합니다. 핀 소유자만 수정할 수 있습니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "핀을 찾을 수 없음")
    })
    @PutMapping("/{pinId}")
    public ApiResponse<PinInfo> updatePin(
            @Parameter(description = "핀 ID", required = true, example = "1")
            @PathVariable Long pinId,
            @Parameter(description = "핀 수정 요청 정보", required = true)
            @Valid @RequestBody UpdatePinRequest request,
            HttpServletRequest httpRequest) {
        Long userId = securityUtil.getUserIdFromRequest(httpRequest);
        PinInfo pinInfo = pinService.updatePin(userId, pinId, request);
        return ApiResponse.success(pinInfo, "핀이 수정되었습니다.");
    }

    @Operation(
        summary = "핀 삭제",
        description = "핀을 삭제합니다. 핀 소유자만 삭제할 수 있습니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "핀을 찾을 수 없음")
    })
    @DeleteMapping("/{pinId}")
    public ApiResponse<Void> deletePin(
            @Parameter(description = "핀 ID", required = true, example = "1")
            @PathVariable Long pinId,
            HttpServletRequest httpRequest) {
        Long userId = securityUtil.getUserIdFromRequest(httpRequest);
        pinService.deletePin(userId, pinId);
        return ApiResponse.success(null, "핀이 삭제되었습니다.");
    }

}
