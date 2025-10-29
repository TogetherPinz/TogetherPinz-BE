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
    public NotificationInfo createNotification(Long userId, CreateNotificationRequest request) {
        // 사용자 조회
        User user = userCacheService.getUserById(userId)
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
        log.info("알림 생성 성공: notificationId={}, userId={}", savedNotification.getId(), userId);

        return NotificationInfo.fromEntity(savedNotification);
    }

    /** 알림 조회 (전체 또는 필터링) */
    public List<NotificationInfo> getNotifications(Long userId, Boolean isRead, String type) {
        List<Notification> notifications;

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
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        // 권한 확인
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("알림 삭제 권한이 없습니다.");
        }

        notificationRepository.delete(notification);
        log.info("알림 삭제 성공: notificationId={}, userId={}", notificationId, userId);
    }

    /** 알림 읽음 처리 */
    @Transactional
    public NotificationInfo markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        // 권한 확인
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("알림 처리 권한이 없습니다.");
        }

        notification.markAsRead();
        Notification updatedNotification = notificationRepository.save(notification);
        log.info("알림 읽음 처리 성공: notificationId={}, userId={}", notificationId, userId);

        return NotificationInfo.fromEntity(updatedNotification);
    }

    /** 위치 기반 알림 트리거 */
    @Transactional
    public List<NotificationInfo> triggerLocationBasedNotifications(Long userId, LocationTriggerRequest request) {
        // 사용자 조회
        User user = userCacheService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 사용자 근처의 핀 조회
        Double radiusKm = 0.5; // 500m
        Double latDelta = radiusKm / 111.0;
        Double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(request.getLatitude())));

        List<Pin> nearbyPins = pinRepository.findPinsNearLocation(
                request.getLatitude() - latDelta,
                request.getLatitude() + latDelta,
                request.getLongitude() - lonDelta,
                request.getLongitude() + lonDelta
        );

        // 사용자의 핀만 필터링
        List<Pin> userPins = nearbyPins.stream()
                .filter(pin -> pin.getUser().getId().equals(userId))
                .toList();

        // 각 핀에 대한 할 일 조회 및 알림 생성
        List<Notification> notifications = userPins.stream()
                .flatMap(pin -> {
                    List<Task> tasks = taskRepository.findByPinIdAndCompleted(pin.getId(), false);
                    return tasks.stream().map(task ->
                            Notification.builder()
                                    .user(user)
                                    .task(task)
                                    .pin(pin)
                                    .title("위치 알림: " + pin.getTitle())
                                    .message("'" + task.getTitle() + "' 할 일이 있습니다.")
                                    .type(NotificationType.LOCATION.name())
                                    .build()
                    );
                })
                .collect(Collectors.toList());

        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
        log.info("위치 기반 알림 트리거 성공: userId={}, 알림 개수={}", userId, savedNotifications.size());

        // WebSocket으로 실시간 알림 전송
        savedNotifications.forEach(notification -> {
            sendPushNotification(userId, NotificationInfo.fromEntity(notification));
        });

        return savedNotifications.stream()
                .map(NotificationInfo::fromEntity)
                .collect(Collectors.toList());
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
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

}
