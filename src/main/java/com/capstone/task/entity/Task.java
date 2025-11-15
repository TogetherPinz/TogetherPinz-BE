package com.capstone.task.entity;

import com.capstone.user.entity.User;
import com.capstone.pin.entity.Pin;
import lombok.*;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Task implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id")
    private Pin pin;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

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
    public Task(String title, Pin pin, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.title = title;
        this.pin = pin;
        this.completed = false;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    /** 할 일 수정 */
    public void updateTask(String title, Pin pin, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (title != null) {
            this.title = title;
        }

        if (pin != null) {
            this.pin = pin;
        }

        if (startDateTime != null) {
            this.startDateTime = startDateTime;
        }

        if (endDateTime != null) {
            this.endDateTime = endDateTime;
        }

    }

    /** 할 일 완료 처리 */
    public void markAsCompleted() {
        this.completed = true;
        this.completedAt = LocalDateTime.now();
    }

    /** 할 일 완료 취소 */
    public void markAsIncomplete() {
        this.completed = false;
        this.completedAt = null;
    }

}
