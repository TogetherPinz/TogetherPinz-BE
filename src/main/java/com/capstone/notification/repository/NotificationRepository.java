package com.capstone.notification.repository;

import com.capstone.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** 사용자 ID로 알림 목록 조회 */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** 사용자 ID와 읽음 여부로 조회 */
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead);

    /** 사용자 ID와 타입으로 조회 */
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type);

    /** 읽지 않은 알림 개수 조회 */
    Long countByUserIdAndIsRead(Long userId, Boolean isRead);

}
