package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.ConversationType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationResponse {

    Long conversationId;

    Long projectId;

    Long clientId;
    String clientName;

    Long expertId;
    String expertName;

    ConversationType conversationType;

    LocalDateTime createAt;
    LocalDateTime updateAt;
}