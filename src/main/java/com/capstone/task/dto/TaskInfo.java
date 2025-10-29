package com.capstone.task.dto;

import com.capstone.task.entity.Task;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskInfo {

    private Long id;
    private String title;
    private String description;
    private Boolean completed;
    private LocalDateTime completedAt;
    private Long userId;
    private String username;
    private Long pinId;
    private String pinTitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskInfo fromEntity(Task task) {
        return TaskInfo.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .completed(task.getCompleted())
                .completedAt(task.getCompletedAt())
                .userId(task.getUser().getId())
                .username(task.getUser().getUsername())
                .pinId(task.getPin() != null ? task.getPin().getId() : null)
                .pinTitle(task.getPin() != null ? task.getPin().getTitle() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

}
