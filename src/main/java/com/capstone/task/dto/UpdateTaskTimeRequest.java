package com.capstone.task.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskTimeRequest {

    private LocalDateTime startTime;

    private LocalDateTime endTime;

}
