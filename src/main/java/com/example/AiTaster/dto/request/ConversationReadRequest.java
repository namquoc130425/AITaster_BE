package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationReadRequest {

    @NotNull(message = "FIELD_REQUIRED")
    Long conversationId;
}