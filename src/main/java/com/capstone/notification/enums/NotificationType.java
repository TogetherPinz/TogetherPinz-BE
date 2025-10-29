package com.capstone.notification.enums;

public enum NotificationType {
    LOCATION("위치 기반 알림"),
    TASK("할 일 알림"),
    SYSTEM("시스템 알림"),
    GROUP("그룹 알림");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
