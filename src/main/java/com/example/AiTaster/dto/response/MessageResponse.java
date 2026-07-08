package com.example.AiTaster.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageResponse {

    Long messageId;

    Long conversationId;

    Long senderId;
    String senderName;
    String senderAvatarUrl;

    Long receiverId;
    String receiverName;
    String receiverAvatarUrl;

    String content;

    Boolean isRead;

    LocalDateTime sendAt;
}
