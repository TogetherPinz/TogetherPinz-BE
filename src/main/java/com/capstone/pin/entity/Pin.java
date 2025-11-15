package com.capstone.pin.entity;

import com.capstone.user.entity.User;
import lombok.*;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "pins")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Pin implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "notification_radius")
    private Integer notificationRadius;

    @Column(name = "current_member_count")
    private Integer currentMemberCount;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @Builder
    public Pin(String title, String address, Double latitude, Double longitude, Integer notificationRadius, Integer currentMemberCount) {
        this.title = title;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.notificationRadius = notificationRadius != null ? notificationRadius : 100;
        this.currentMemberCount = currentMemberCount != null ? currentMemberCount : 1;
    }

    /** 핀 정보 수정 */
    public void updatePin(String title, String address, Double latitude, Double longitude, Integer notificationRadius) {
        if (title != null) {
            this.title = title;
        }
        if (address != null) {
            this.address = address;
        }
        if (latitude != null) {
            this.latitude = latitude;
        }
        if (longitude != null) {
            this.longitude = longitude;
        }
        if (notificationRadius != null) {
            this.notificationRadius = notificationRadius;
        }
    }

    /** 멤버 수 증가 */
    public void incrementMemberCount() {
        this.currentMemberCount++;
    }

    /** 멤버 수 감소 */
    public void decrementMemberCount() {
        if (this.currentMemberCount > 0) {
            this.currentMemberCount--;
        }
    }

}
