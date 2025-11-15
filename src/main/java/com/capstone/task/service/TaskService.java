package com.capstone.task.service;

import com.capstone.pin.entity.Pin;
import com.capstone.pin.repository.PinRepository;
import com.capstone.task.dto.*;
import com.capstone.task.entity.Task;
import com.capstone.task.repository.TaskRepository;
import com.capstone.user.entity.User;
import com.capstone.user.service.UserCacheService;
import com.capstone.member.entity.Member;
import com.capstone.member.service.MemberCacheService;
import com.capstone.member.enums.MemberRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserCacheService userCacheService;
    private final PinRepository pinRepository;
    private final MemberCacheService memberCacheService;

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
                .pin(pin)
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .build();

        Task savedTask = taskRepository.save(task);
        log.info("할 일 생성 성공: taskId={}, userId={}", savedTask.getId(), userId);

        return TaskInfo.fromEntity(savedTask);
    }

    /** 할 일 조회 */
    public List<TaskInfo> getTasks(Long userId, Long pinId) {

        Member member = memberCacheService.getMemberByPinIdAndUserId(pinId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 핀의 멤버가 아닙니다."));

        List<Task> tasks = taskRepository.findByPinId(pinId);

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
        Member requester = memberCacheService.getMemberByPinIdAndUserId(request.getPinId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 핀의 멤버가 아닙니다."));

        if (requester.getRole() != MemberRole.OWNER) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("할 일을 찾을 수 없거나 수정 권한이 없습니다."));

        // 핀 조회 (변경 시)
        Pin pin = null;

        if (request.getPinId() != null) {
            pin = pinRepository.findById(request.getPinId())
                    .orElseThrow(() -> new IllegalArgumentException("핀을 찾을 수 없습니다."));
        }

        // 할 일 수정
        task.updateTask(request.getTitle(), pin, request.getStartDateTime(), request.getEndDateTime());
        Task updatedTask = taskRepository.save(task);
        log.info("할 일 수정 성공: taskId={}, userId={}", taskId, userId);

        return TaskInfo.fromEntity(updatedTask);
    }

    /** 할 일 삭제 */
    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        // 할 일 조회 및 권한 확인
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("할 일을 찾을 수 없습니다."));

        Member requester = memberCacheService.getMemberByPinIdAndUserId(task.getPin().getId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 핀의 멤버가 아닙니다."));

        if (requester.getRole() != MemberRole.OWNER) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        taskRepository.delete(task);
        log.info("할 일 삭제 성공: taskId={}, userId={}", taskId, userId);
    }

    /** 할 일 완료 처리 */
    @Transactional
    public TaskInfo completeTask(Long userId, Long taskId) {
        // 할 일 조회 및 권한 확인
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("할 일을 찾을 수 없습니다."));

        Member requester = memberCacheService.getMemberByPinIdAndUserId(task.getPin().getId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 핀의 멤버가 아닙니다."));

        task.markAsCompleted();
        Task completedTask = taskRepository.save(task);
        log.info("할 일 완료 처리 성공: taskId={}, userId={}", taskId, userId);

        return TaskInfo.fromEntity(completedTask);
    }

}
