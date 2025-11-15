package com.capstone.task.controller;

import com.capstone.common.dto.ApiResponse;
import com.capstone.common.util.SecurityUtil;
import com.capstone.task.dto.*;
import com.capstone.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "할 일(Task)", description = "할 일 관리 API")
public class TaskController {

    private final TaskService taskService;
    private final SecurityUtil securityUtil;

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
            @Parameter(description = "할 일 생성 요청 정보", required = true)
            @Valid @RequestBody CreateTaskRequest request,
            HttpServletRequest httpServletRequest) {
        Long userId = securityUtil.getUserIdFromRequest(httpServletRequest);
        TaskInfo taskInfo = taskService.createTask(userId, request);
        return ApiResponse.success(taskInfo, "할 일이 생성되었습니다.");
    }

    @Operation(
        summary = "할 일 목록 조회",
        description = "pinId에 해당하는 할 일 목록을 조회합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/{pinId}")
    public ApiResponse<List<TaskInfo>> getTasks(
            @Parameter(description = "핀 ID", example = "1")
            @PathVariable Long pinId,
            HttpServletRequest httpServletRequest) {
        Long userId = securityUtil.getUserIdFromRequest(httpServletRequest);
        List<TaskInfo> tasks = taskService.getTasks(userId, pinId);
        return ApiResponse.success(tasks);
    }

    /** 할 일 수정 */
    @Operation(
            summary = "할 일 수정",
            description = "taskId에 해당하는 할 일을 수정합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공")
    })
    @PutMapping("/{taskId}")
    public ApiResponse<TaskInfo> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            HttpServletRequest httpServletRequest) {
        Long userId = securityUtil.getUserIdFromRequest(httpServletRequest);
        TaskInfo taskInfo = taskService.updateTask(userId, taskId, request);
        return ApiResponse.success(taskInfo, "할 일이 수정되었습니다.");
    }

    /** 할 일 삭제 */
    @Operation(
            summary = "할 일 삭제",
            description = "taskId에 해당하는 할 일을 삭제합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공")
    })
    @DeleteMapping("/{taskId}")
    public ApiResponse<Void> deleteTask(
            @PathVariable Long taskId,
            HttpServletRequest httpServletRequest) {
        Long userId = securityUtil.getUserIdFromRequest(httpServletRequest);
        taskService.deleteTask(userId, taskId);
        return ApiResponse.success(null, "할 일이 삭제되었습니다.");
    }

    /** 할 일 완료 처리 */
    @Operation(
            summary = "할 일 완료 처리",
            description = "taskId에 해당하는 할 일을 완료 처리합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "처리 성공")
    })
    @PostMapping("/{taskId}/complete")
    public ApiResponse<TaskInfo> completeTask(
            @PathVariable Long taskId,
            HttpServletRequest httpServletRequest) {
        Long userId = securityUtil.getUserIdFromRequest(httpServletRequest);
        TaskInfo taskInfo = taskService.completeTask(userId, taskId);
        return ApiResponse.success(taskInfo, "할 일이 완료되었습니다.");
    }

}
