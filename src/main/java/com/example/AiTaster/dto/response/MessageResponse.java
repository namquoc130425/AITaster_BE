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

    Long receiverId;
    String receiverName;

    String content;

    Boolean isRead;

    LocalDateTime sendAt;
}