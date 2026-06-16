package com.example.AiTaster.dto.request;

import com.example.AiTaster.constant.BlockedContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BlockedContentRequest {
    @NotBlank(message = "FIELD_REQUIRED")
    String content;

    @NotNull(message = "FIELD_REQUIRED")
    BlockedContentType type;
}
