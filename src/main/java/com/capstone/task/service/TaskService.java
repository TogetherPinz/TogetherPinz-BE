package com.capstone.task.service;

import com.capstone.pin.entity.Pin;
import com.capstone.pin.repository.PinRepository;
import com.capstone.task.dto.*;
import com.capstone.task.entity.Task;
import com.capstone.task.entity.TaskTimeInfo;
import com.capstone.task.repository.TaskRepository;
import com.capstone.task.repository.TaskTimeInfoRepository;
import com.capstone.user.entity.User;
import com.capstone.user.service.UserCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskTimeInfoRepository taskTimeInfoRepository;
    private final UserCacheService userCacheService;
    private final PinRepository pinRepository;

    /** 할 일 생성 */
    @Transactional
    public TaskInfo createTask(Long userId, CreateTaskRequest request) {
        // 사용자 조회
        User user = userCacheService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 핀 조회 (선택)
        Pin pin = null;
        if (request.getPinId() != null) {
            pin = pinRepository.findById(request.getPinId())
                    .orElseThrow(() -> new IllegalArgumentException("핀을 찾을 수 없습니다."));
        }

        // 할 일 생성
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .user(user)
                .pin(pin)
                .build();

        Task savedTask = taskRepository.save(task);
        log.info("할 일 생성 성공: taskId={}, userId={}", savedTask.getId(), userId);

        return TaskInfo.fromEntity(savedTask);
    }

    /** 할 일 조회 (전체 또는 사용자별/핀별/완료여부별) */
    public List<TaskInfo> getTasks(Long userId, Long pinId, Boolean completed) {
        List<Task> tasks;

        if (userId != null && completed != null) {
            tasks = taskRepository.findByUserIdAndCompleted(userId, completed);
        } else if (userId != null) {
            tasks = taskRepository.findByUserId(userId);
        } else if (pinId != null && completed != null) {
            tasks = taskRepository.findByPinIdAndCompleted(pinId, completed);
        } else if (pinId != null) {
            tasks = taskRepository.findByPinId(pinId);
        } else {
            tasks = taskRepository.findAll();
        }

        return tasks.stream()
                .map(TaskInfo::fromEntity)
                .collect(Collectors.toList());
    }

    /** 할 일 단건 조회 */
    public TaskInfo getTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("할 일을 찾을 수 없습니다."));
        return TaskInfo.fromEntity(task);
    }

    /** 할 일 수정 */
    @Transactional
    public TaskInfo updateTask(Long userId, Long taskId, UpdateTaskRequest request) {
        // 할 일 조회 및 권한 확인
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("할 일을 찾을 수 없거나 수정 권한이 없습니다."));

        // 핀 조회 (변경 시)
        Pin pin = null;
        if (request.getPinId() != null) {
            pin = pinRepository.findById(request.getPinId())
                    .orElseThrow(() -> new IllegalArgumentException("핀을 찾을 수 없습니다."));
        }

        // 할 일 수정
        task.updateTask(request.getTitle(), request.getDescription(), pin);
        Task updatedTask = taskRepository.save(task);
        log.info("할 일 수정 성공: taskId={}, userId={}", taskId, userId);

        return TaskInfo.fromEntity(updatedTask);
    }

    /** 할 일 삭제 */
    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        // 할 일 조회 및 권한 확인
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("할 일을 찾을 수 없거나 삭제 권한이 없습니다."));

        taskRepository.delete(task);
        log.info("할 일 삭제 성공: taskId={}, userId={}", taskId, userId);
    }

    /** 할 일 완료 처리 */
    @Transactional
    public TaskInfo completeTask(Long userId, Long taskId) {
        // 할 일 조회 및 권한 확인
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("할 일을 찾을 수 없거나 권한이 없습니다."));

        task.markAsCompleted();
        Task completedTask = taskRepository.save(task);
        log.info("할 일 완료 처리 성공: taskId={}, userId={}", taskId, userId);

        return TaskInfo.fromEntity(completedTask);
    }

    /** 할 일 시간 정보 생성 */
    @Transactional
    public TaskTimeInfoDto createTaskTime(Long userId, Long taskId, CreateTaskTimeRequest request) {
        // 할 일 조회 및 권한 확인
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("할 일을 찾을 수 없거나 권한이 없습니다."));

        // 시간 검증
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 이후여야 합니다.");
        }

        // 시간 정보 생성
        TaskTimeInfo timeInfo = TaskTimeInfo.builder()
                .task(task)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        TaskTimeInfo savedTimeInfo = taskTimeInfoRepository.save(timeInfo);
        log.info("할 일 시간 정보 생성 성공: taskId={}, timeInfoId={}", taskId, savedTimeInfo.getId());

        return TaskTimeInfoDto.fromEntity(savedTimeInfo);
    }

    /** 할 일 시간 정보 조회 */
    public List<TaskTimeInfoDto> getTaskTimes(Long taskId) {
        List<TaskTimeInfo> timeInfos = taskTimeInfoRepository.findByTaskId(taskId);
        return timeInfos.stream()
                .map(TaskTimeInfoDto::fromEntity)
                .collect(Collectors.toList());
    }

    /** 할 일 시간 정보 수정 */
    @Transactional
    public TaskTimeInfoDto updateTaskTime(Long userId, Long taskId, Long timeId, UpdateTaskTimeRequest request) {
        // 할 일 조회 및 권한 확인
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("할 일을 찾을 수 없거나 권한이 없습니다."));

        // 시간 정보 조회
        TaskTimeInfo timeInfo = taskTimeInfoRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException("시간 정보를 찾을 수 없습니다."));

        // 할 일 ID 일치 확인
        if (!timeInfo.getTask().getId().equals(taskId)) {
            throw new IllegalArgumentException("시간 정보가 해당 할 일에 속하지 않습니다.");
        }

        // 시간 정보 수정
        timeInfo.updateTimeInfo(request.getStartTime(), request.getEndTime());
        TaskTimeInfo updatedTimeInfo = taskTimeInfoRepository.save(timeInfo);
        log.info("할 일 시간 정보 수정 성공: taskId={}, timeInfoId={}", taskId, timeId);

        return TaskTimeInfoDto.fromEntity(updatedTimeInfo);
    }

    /** 할 일 시간 정보 삭제 */
    @Transactional
    public void deleteTaskTime(Long userId, Long taskId, Long timeId) {
        // 할 일 조회 및 권한 확인
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("할 일을 찾을 수 없거나 권한이 없습니다."));

        // 시간 정보 조회
        TaskTimeInfo timeInfo = taskTimeInfoRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException("시간 정보를 찾을 수 없습니다."));

        // 할 일 ID 일치 확인
        if (!timeInfo.getTask().getId().equals(taskId)) {
            throw new IllegalArgumentException("시간 정보가 해당 할 일에 속하지 않습니다.");
        }

        taskTimeInfoRepository.delete(timeInfo);
        log.info("할 일 시간 정보 삭제 성공: taskId={}, timeInfoId={}", taskId, timeId);
    }

}
