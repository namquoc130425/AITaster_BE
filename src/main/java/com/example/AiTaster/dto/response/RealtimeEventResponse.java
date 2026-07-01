package com.example.AiTaster.dto.response;

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
public class RealtimeEventResponse {
    String eventType;
    ReferenceType referenceType;
    Long referenceId;
    Long userId;
    Long projectId;
    Long walletId;
    Long conversationId;
    String message;
    LocalDateTime at;
}
