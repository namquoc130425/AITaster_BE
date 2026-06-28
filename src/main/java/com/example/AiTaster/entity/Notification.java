package com.example.AiTaster.entity;

import com.example.AiTaster.constant.NotificationType;
import com.example.AiTaster.constant.ReferenceType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notification",
        indexes = {
                @Index(name = "idx_notification_user", columnList = "user_id"),
                @Index(name = "idx_notification_is_read", columnList = "is_read"),
                @Index(name = "idx_notification_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(nullable = false)
    String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    ReferenceType referenceType;

    Long referenceId;

    @Column(name = "is_read", nullable = false)
    Boolean isRead;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (isRead == null) {
            isRead = false;
        }

        if (referenceType == null) {
            referenceType = ReferenceType.NONE;
        }
    }
}