package com.capstone.notification.dto;

import com.capstone.notification.entity.Notification;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationInfo {

    private Long id;
    private String title;
    private String message;
    private String type;
    private Boolean isRead;
    private LocalDateTime readAt;
    private Long userId;
    private String username;
    private Long taskId;
    private String taskTitle;
    private Long pinId;
    private String pinTitle;
    private LocalDateTime createdAt;

    public static NotificationInfo fromEntity(Notification notification) {
        return NotificationInfo.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .userId(notification.getUser().getId())
                .username(notification.getUser().getUsername())
                .taskId(notification.getTask() != null ? notification.getTask().getId() : null)
                .taskTitle(notification.getTask() != null ? notification.getTask().getTitle() : null)
                .pinId(notification.getPin() != null ? notification.getPin().getId() : null)
                .pinTitle(notification.getPin() != null ? notification.getPin().getTitle() : null)
                .createdAt(notification.getCreatedAt())
                .build();
    }

}
