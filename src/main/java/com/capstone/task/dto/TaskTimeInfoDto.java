package com.capstone.task.dto;

import com.capstone.task.entity.TaskTimeInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskTimeInfoDto {

    private Long id;
    private Long taskId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskTimeInfoDto fromEntity(TaskTimeInfo entity) {
        return TaskTimeInfoDto.builder()
                .id(entity.getId())
                .taskId(entity.getTask().getId())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

}
