package com.capstone.task.controller;

import com.capstone.common.dto.ApiResponse;
import com.capstone.task.dto.*;
import com.capstone.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
@Tag(name = "할 일(Task)", description = "할 일 관리 API")
public class TaskController {

    private final TaskService taskService;

    @Operation(
        summary = "할 일 생성",
        description = "새로운 할 일을 생성합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "할 일 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TaskInfo> createTask(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestParam Long userId,
            @Parameter(description = "할 일 생성 요청 정보", required = true)
            @Valid @RequestBody CreateTaskRequest request) {
        TaskInfo taskInfo = taskService.createTask(userId, request);
        return ApiResponse.success(taskInfo, "할 일이 생성되었습니다.");
    }

    @Operation(
        summary = "할 일 목록 조회",
        description = "필터 조건에 맞는 할 일 목록을 조회합니다. 모든 필터는 선택사항입니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ApiResponse<List<TaskInfo>> getTasks(
            @Parameter(description = "사용자 ID (선택사항)", example = "1")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "핀 ID (선택사항)", example = "1")
            @RequestParam(required = false) Long pinId,
            @Parameter(description = "완료 여부 (선택사항)", example = "false")
            @RequestParam(required = false) Boolean completed) {
        List<TaskInfo> tasks = taskService.getTasks(userId, pinId, completed);
        return ApiResponse.success(tasks);
    }

    /** 할 일 단건 조회 */
    @GetMapping("/{id}")
    public ApiResponse<TaskInfo> getTask(@PathVariable Long id) {
        TaskInfo taskInfo = taskService.getTask(id);
        return ApiResponse.success(taskInfo);
    }

    /** 할 일 수정 */
    @PutMapping("/{id}")
    public ApiResponse<TaskInfo> updateTask(
            @PathVariable Long id,
            @RequestParam Long userId,
            @Valid @RequestBody UpdateTaskRequest request) {
        TaskInfo taskInfo = taskService.updateTask(userId, id, request);
        return ApiResponse.success(taskInfo, "할 일이 수정되었습니다.");
    }

    /** 할 일 삭제 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTask(
            @PathVariable Long id,
            @RequestParam Long userId) {
        taskService.deleteTask(userId, id);
        return ApiResponse.success(null, "할 일이 삭제되었습니다.");
    }

    /** 할 일 완료 처리 */
    @PostMapping("/{id}/complete")
    public ApiResponse<TaskInfo> completeTask(
            @PathVariable Long id,
            @RequestParam Long userId) {
        TaskInfo taskInfo = taskService.completeTask(userId, id);
        return ApiResponse.success(taskInfo, "할 일이 완료되었습니다.");
    }

    /** 할 일 시간 정보 생성 */
    @PostMapping("/{id}/time")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TaskTimeInfoDto> createTaskTime(
            @PathVariable Long id,
            @RequestParam Long userId,
            @Valid @RequestBody CreateTaskTimeRequest request) {
        TaskTimeInfoDto timeInfo = taskService.createTaskTime(userId, id, request);
        return ApiResponse.success(timeInfo, "시간 정보가 생성되었습니다.");
    }

    /** 할 일 시간 정보 조회 */
    @GetMapping("/{id}/time")
    public ApiResponse<List<TaskTimeInfoDto>> getTaskTimes(@PathVariable Long id) {
        List<TaskTimeInfoDto> timeInfos = taskService.getTaskTimes(id);
        return ApiResponse.success(timeInfos);
    }

    /** 할 일 시간 정보 수정 */
    @PutMapping("/{id}/time/{timeId}")
    public ApiResponse<TaskTimeInfoDto> updateTaskTime(
            @PathVariable Long id,
            @PathVariable Long timeId,
            @RequestParam Long userId,
            @Valid @RequestBody UpdateTaskTimeRequest request) {
        TaskTimeInfoDto timeInfo = taskService.updateTaskTime(userId, id, timeId, request);
        return ApiResponse.success(timeInfo, "시간 정보가 수정되었습니다.");
    }

    /** 할 일 시간 정보 삭제 */
    @DeleteMapping("/{id}/time/{timeId}")
    public ApiResponse<Void> deleteTaskTime(
            @PathVariable Long id,
            @PathVariable Long timeId,
            @RequestParam Long userId) {
        taskService.deleteTaskTime(userId, id, timeId);
        return ApiResponse.success(null, "시간 정보가 삭제되었습니다.");
    }

}
