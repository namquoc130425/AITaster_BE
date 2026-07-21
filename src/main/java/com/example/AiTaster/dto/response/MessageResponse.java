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
<<<<<<< HEAD

    Long receiverId;
    String receiverName;
=======
    String senderAvatarUrl;

    Long receiverId;
    String receiverName;
    String receiverAvatarUrl;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384

    String content;

    Boolean isRead;

    LocalDateTime sendAt;
<<<<<<< HEAD
}
=======
}
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
