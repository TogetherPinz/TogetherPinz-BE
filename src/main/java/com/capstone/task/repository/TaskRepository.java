package com.capstone.task.repository;

import com.capstone.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /** 사용자 ID로 할 일 목록 조회 */
    List<Task> findByUserId(Long userId);

    /** 사용자 ID와 할 일 ID로 조회 (권한 확인용) */
    Optional<Task> findByIdAndUserId(Long taskId, Long userId);

    /** 핀 ID로 할 일 목록 조회 */
    List<Task> findByPinId(Long pinId);

    /** 사용자 ID와 완료 여부로 조회 */
    List<Task> findByUserIdAndCompleted(Long userId, Boolean completed);

    /** 핀 ID와 완료 여부로 조회 */
    List<Task> findByPinIdAndCompleted(Long pinId, Boolean completed);

}
