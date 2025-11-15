package com.capstone.task.repository;

import com.capstone.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /** 핀 ID로 할 일 목록 조회 */
    List<Task> findByPinId(Long pinId);

}
