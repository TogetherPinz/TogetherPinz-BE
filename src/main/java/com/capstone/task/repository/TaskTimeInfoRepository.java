package com.capstone.task.repository;

import com.capstone.task.entity.TaskTimeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskTimeInfoRepository extends JpaRepository<TaskTimeInfo, Long> {

    /** 할 일 ID로 시간 정보 목록 조회 */
    List<TaskTimeInfo> findByTaskId(Long taskId);

}
