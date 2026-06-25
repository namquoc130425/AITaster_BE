package com.example.AiTaster.dto.request;

import com.example.AiTaster.constant.MessageType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageRequest {

    @NotNull(message = "FIELD_REQUIRED")
    Long conversationId;

    String content;

    String fileUrl;

    MessageType messageType;
}