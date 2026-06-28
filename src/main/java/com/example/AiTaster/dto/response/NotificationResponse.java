package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.NotificationType;
import com.example.AiTaster.constant.ReferenceType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {

    Long notificationId;

    Long userId;

    String title;

    String content;

    NotificationType notificationType;

    ReferenceType referenceType;

    Long referenceId;

    Boolean isRead;

    LocalDateTime createdAt;
}