package com.example.AiTaster.event;

import com.example.AiTaster.constant.NotificationType;
import com.example.AiTaster.constant.ReferenceType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationCreatedEvent {

    Long receiverUserId;

    String title;

    String content;

    NotificationType notificationType;

    ReferenceType referenceType;

    Long referenceId;
}