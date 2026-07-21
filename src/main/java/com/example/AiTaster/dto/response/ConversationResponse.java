package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.ConversationType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationResponse {

    Long conversationId;

    Long applicationId;

    Long jobPostId;

    Long projectId;

    Long clientId;
    String clientName;
    String clientAvatarUrl;

    Long expertId;
    String expertName;
    String expertAvatarUrl;

    ConversationType conversationType;

    Long unreadCount;

    LocalDateTime convertedToProjectAt;

    LocalDateTime createAt;

    LocalDateTime updateAt;
}
