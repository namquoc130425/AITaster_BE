package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageRequest {

    @NotNull(message = "FIELD_REQUIRED")
    Long conversationId;

    @NotBlank(message = "MESSAGE_CONTENT_REQUIRED")
    String content;
}