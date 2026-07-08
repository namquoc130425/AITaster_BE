package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpertServiceRejectRequest {

    @NotBlank(message = "FIELD_REQUIRED")
    String rejectionReason;
}