package com.capstone.notification.controller;

import com.capstone.common.dto.ApiResponse;
import com.capstone.notification.dto.*;
import com.capstone.notification.service.NotificationService;
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
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Tag(name = "알림(Notification)", description = "알림 관리 API")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
        summary = "알림 생성",
        description = "새로운 알림을 생성합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "알림 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NotificationInfo> createNotification(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestParam Long userId,
            @Parameter(description = "알림 생성 요청 정보", required = true)
            @Valid @RequestBody CreateNotificationRequest request) {
        NotificationInfo notificationInfo = notificationService.createNotification(userId, request);
        return ApiResponse.success(notificationInfo, "알림이 생성되었습니다.");
    }

    @Operation(
        summary = "알림 목록 조회",
        description = "사용자의 알림 목록을 조회합니다. 읽음 여부와 타입으로 필터링할 수 있습니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ApiResponse<List<NotificationInfo>> getNotifications(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestParam Long userId,
            @Parameter(description = "읽음 여부 (선택사항)", example = "false")
            @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "알림 타입 (선택사항)", example = "LOCATION")
            @RequestParam(required = false) String type) {
        List<NotificationInfo> notifications = notificationService.getNotifications(userId, isRead, type);
        return ApiResponse.success(notifications);
    }

    @Operation(
        summary = "알림 단건 조회",
        description = "특정 알림의 상세 정보를 조회합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ApiResponse<NotificationInfo> getNotification(
            @Parameter(description = "알림 ID", required = true, example = "1")
            @PathVariable Long id) {
        NotificationInfo notificationInfo = notificationService.getNotification(id);
        return ApiResponse.success(notificationInfo);
    }

    @Operation(
        summary = "알림 삭제",
        description = "특정 알림을 삭제합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNotification(
            @Parameter(description = "알림 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestParam Long userId) {
        notificationService.deleteNotification(userId, id);
        return ApiResponse.success(null, "알림이 삭제되었습니다.");
    }

    @Operation(
        summary = "알림 읽음 처리",
        description = "특정 알림을 읽음 상태로 변경합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "읽음 처리 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    @PostMapping("/{id}/read")
    public ApiResponse<NotificationInfo> markAsRead(
            @Parameter(description = "알림 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestParam Long userId) {
        NotificationInfo notificationInfo = notificationService.markAsRead(userId, id);
        return ApiResponse.success(notificationInfo, "알림을 읽음 처리했습니다.");
    }

    @Operation(
        summary = "위치 기반 알림 트리거",
        description = "사용자의 현재 위치를 기반으로 해당 위치에 있는 핀의 할 일에 대한 알림을 생성합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "알림 트리거 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/trigger")
    public ApiResponse<List<NotificationInfo>> triggerLocationNotifications(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestParam Long userId,
            @Parameter(description = "위치 정보", required = true)
            @Valid @RequestBody LocationTriggerRequest request) {
        List<NotificationInfo> notifications = notificationService.triggerLocationBasedNotifications(userId, request);
        return ApiResponse.success(notifications, notifications.size() + "개의 알림이 생성되었습니다.");
    }

    @Operation(
        summary = "푸시 알림 전송",
        description = "사용자에게 푸시 알림을 전송합니다. WebSocket을 통해 실시간으로 전달됩니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "푸시 알림 전송 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/push")
    public ApiResponse<Void> sendPushNotification(
            @Parameter(description = "푸시 알림 요청 정보", required = true)
            @Valid @RequestBody PushNotificationRequest request) {
        NotificationInfo notificationInfo = NotificationInfo.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .build();
        
        notificationService.sendPushNotification(request.getUserId(), notificationInfo);
        return ApiResponse.success(null, "푸시 알림이 전송되었습니다.");
    }

    @Operation(
        summary = "읽지 않은 알림 개수 조회",
        description = "사용자의 읽지 않은 알림 개수를 조회합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestParam Long userId) {
        Long count = notificationService.getUnreadCount(userId);
        return ApiResponse.success(count);
    }

}
