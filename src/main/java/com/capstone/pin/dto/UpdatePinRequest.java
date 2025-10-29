package com.capstone.pin.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePinRequest {

    @Size(max = 100, message = "제목은 100자 이하여야 합니다")
    private String title;

    @Size(max = 500, message = "설명은 500자 이하여야 합니다")
    private String description;

    private Double latitude;

    private Double longitude;

    private Integer notificationRadius;

}
