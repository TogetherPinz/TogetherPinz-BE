package com.capstone.notification.service;

import com.capstone.notification.dto.*;
import com.capstone.notification.entity.Notification;
import com.capstone.notification.enums.NotificationType;
import com.capstone.notification.repository.NotificationRepository;
import com.capstone.pin.entity.Pin;
import com.capstone.pin.repository.PinRepository;
import com.capstone.task.entity.Task;
import com.capstone.task.repository.TaskRepository;
import com.capstone.user.entity.User;
import com.capstone.user.service.UserCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserCacheService userCacheService;
    private final TaskRepository taskRepository;
    private final PinRepository pinRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /** 알림 생성 */
    @Transactional
    public NotificationInfo createNotification(String username, CreateNotificationRequest request) {
        // 사용자 조회
        User user = userCacheService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // Task 조회 (선택)
        Task task = null;
        if (request.getTaskId() != null) {
            task = taskRepository.findById(request.getTaskId())
                    .orElse(null);
        }

        // Pin 조회 (선택)
        Pin pin = null;
        if (request.getPinId() != null) {
            pin = pinRepository.findById(request.getPinId())
                    .orElse(null);
        }

        // 알림 생성
        Notification notification = Notification.builder()
                .user(user)
                .task(task)
                .pin(pin)
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("알림 생성 성공: notificationId={}, userId={}", savedNotification.getId(), user.getId());

        return NotificationInfo.fromEntity(savedNotification);
    }

    /** 알림 조회 (전체 또는 필터링) */
    public List<NotificationInfo> getNotifications(String username, Boolean isRead, String type) {
        List<Notification> notifications;

        User user = userCacheService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Long userId = user.getId();

        if (isRead != null) {
            notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, isRead);
        } else if (type != null) {
            notifications = notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type);
        } else {
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        return notifications.stream()
                .map(NotificationInfo::fromEntity)
                .collect(Collectors.toList());
    }

    /** 알림 단건 조회 */
    public NotificationInfo getNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));
        return NotificationInfo.fromEntity(notification);
    }

    /** 알림 삭제 */
    @Transactional
    public void deleteNotification(String username, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        User user = userCacheService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Long userId = user.getId();

        // 권한 확인
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("알림 삭제 권한이 없습니다.");
        }

        notificationRepository.delete(notification);
        log.info("알림 삭제 성공: notificationId={}, userId={}", notificationId, userId);
    }

    /** 알림 읽음 처리 */
    @Transactional
    public NotificationInfo markAsRead(String username, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        User user = userCacheService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 권한 확인
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("알림 처리 권한이 없습니다.");
        }

        notification.markAsRead();
        Notification updatedNotification = notificationRepository.save(notification);
        log.info("알림 읽음 처리 성공: notificationId={}, userId={}", notificationId, username);

        return NotificationInfo.fromEntity(updatedNotification);
    }

    /** 푸시 알림 전송 (WebSocket) */
    @Transactional
    public void sendPushNotification(Long userId, NotificationInfo notificationInfo) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    notificationInfo
            );
            log.info("푸시 알림 전송 성공: userId={}, notificationId={}", userId, notificationInfo.getId());
        } catch (Exception e) {
            log.error("푸시 알림 전송 실패: userId={}, error={}", userId, e.getMessage());
        }
    }

    /** 읽지 않은 알림 개수 조회 */
    public Long getUnreadCount(String username) {
        User user = userCacheService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return notificationRepository.countByUserIdAndIsRead(user.getId(), false);
    }

}
