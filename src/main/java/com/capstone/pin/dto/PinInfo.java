package com.capstone.pin.dto;

import com.capstone.pin.entity.Pin;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer notificationRadius;
    private Integer currentMemberCount;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime updatedAt;

    public static PinInfo fromEntity(Pin pin) {
        return PinInfo.builder()
                .id(pin.getId())
                .title(pin.getTitle())
                .address(pin.getAddress())
                .latitude(pin.getLatitude())
                .longitude(pin.getLongitude())
                .notificationRadius(pin.getNotificationRadius())
                .currentMemberCount(pin.getCurrentMemberCount())
                .createdAt(pin.getCreatedAt())
                .updatedAt(pin.getUpdatedAt())
                .build();
    }

}
