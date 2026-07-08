package com.example.AiTaster.repository;

import com.example.AiTaster.entity.Notification;
import com.example.AiTaster.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NotificationRepo extends JpaRepository<Notification, Long> {

    Optional<Notification> findByNotificationId(Long notificationId);

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    long countByUserAndIsReadFalse(User user);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Notification n
            set n.isRead = true
            where n.user = :user
              and n.isRead = false
            """)
    int markAllAsReadByUser(User user);
}