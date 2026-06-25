package com.example.AiTaster.dto.request;

import com.example.AiTaster.constant.ConversationType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConversationCreateRequest {

    Long projectId;

    @NotNull(message = "FIELD_REQUIRED")
    Long clientId;

    @NotNull(message = "FIELD_REQUIRED")
    Long expertId;

    @NotNull(message = "FIELD_REQUIRED")
    ConversationType conversationType;
}