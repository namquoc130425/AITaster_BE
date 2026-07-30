package com.example.AiTaster.dto.request;

import com.example.AiTaster.constant.DisputeDecision;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResolveDisputeRequest {

    @NotNull(message = "Decision is required")
    DisputeDecision decision;

    BigDecimal refundAmount;
    BigDecimal releaseAmount;

    String response;

}
