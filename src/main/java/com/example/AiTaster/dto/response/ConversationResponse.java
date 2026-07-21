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
<<<<<<< HEAD

    Long expertId;
    String expertName;

    ConversationType conversationType;

=======
    String clientAvatarUrl;

    Long expertId;
    String expertName;
    String expertAvatarUrl;

    ConversationType conversationType;

    Long unreadCount;

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    LocalDateTime convertedToProjectAt;

    LocalDateTime createAt;

    LocalDateTime updateAt;
<<<<<<< HEAD
}
=======
}
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
