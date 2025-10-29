package com.capstone.task.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {

    @Size(max = 200, message = "제목은 200자 이하여야 합니다")
    private String title;

    private String description;

    private Long pinId;

}
