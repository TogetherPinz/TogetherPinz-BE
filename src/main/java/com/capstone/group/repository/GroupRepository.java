package com.capstone.group.repository;

import com.capstone.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    /** 특정 핀에 속한 모든 그룹 멤버 조회 */
    List<Group> findByPinId(Long pinId);

    /** 특정 사용자가 속한 모든 그룹 조회 */
    List<Group> findByUserId(Long userId);

    /** 특정 핀에 속한 특정 사용자의 그룹 조회 */
    Optional<Group> findByPinIdAndUserId(Long pinId, Long userId);

    /** 특정 핀의 멤버 수 조회 */
    @Query("SELECT COUNT(g) FROM Group g WHERE g.pin.id = :pinId")
    Long countByPinId(@Param("pinId") Long pinId);

    /** 특정 사용자가 특정 핀에 속해있는지 확인 */
    boolean existsByPinIdAndUserId(Long pinId, Long userId);
}
