package com.example.AiTaster.dto.request;

import com.example.AiTaster.constant.NotificationType;
import com.example.AiTaster.constant.ReferenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationCreateRequest {

    @NotBlank(message = "FIELD_REQUIRED")
    String title;

    @NotBlank(message = "FIELD_REQUIRED")
    String content;

    @NotNull(message = "FIELD_REQUIRED")
    NotificationType notificationType;

    @NotNull(message = "FIELD_REQUIRED")
    ReferenceType referenceType;

    Long referenceId;
}