package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.MessageType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageResponse {

    Long messageId;

    Long conversationId;

    Long senderId;
    String senderName;

    Long receiverId;
    String receiverName;

    String content;

    MessageType messageType;

    String fileUrl;

    Boolean isRead;

    LocalDateTime sendAt;
}