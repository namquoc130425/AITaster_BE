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
public class ProjectRealtimeEventResponse {
    String eventType;
    Long projectId;
    Long invitationId;
    Long jobPostId;
    Long targetUserId;
    String message;
    LocalDateTime at;
}
