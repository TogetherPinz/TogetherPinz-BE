package com.capstone.pin.dto;

import com.capstone.pin.entity.Pin;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PinInfo {

    private Long id;
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private Long userId;
    private String username;
    private Integer notificationRadius;
    private Integer currentMemberCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PinInfo fromEntity(Pin pin) {
        return PinInfo.builder()
                .id(pin.getId())
                .title(pin.getTitle())
                .description(pin.getDescription())
                .latitude(pin.getLatitude())
                .longitude(pin.getLongitude())
                .userId(pin.getUser().getId())
                .username(pin.getUser().getUsername())
                .notificationRadius(pin.getNotificationRadius())
                .currentMemberCount(pin.getCurrentMemberCount())
                .createdAt(pin.getCreatedAt())
                .updatedAt(pin.getUpdatedAt())
                .build();
    }

}
