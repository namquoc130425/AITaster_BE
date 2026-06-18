package com.example.AiTaster.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpertApplicationRequest {
    @NotNull(message = "FIELD_REQUIRED")
    @DecimalMin(value = "0.0", inclusive = true, message = "PRICE_INVALID")
    //inclusive = true -> dk phải lớn hơn 0.0
    BigDecimal expectedPrice;

    @NotBlank(message = "FIELD_REQUIRED")
    String estimatedTimeline;

    String shortMessage;

    @Valid
    ExpertProposalRequest proposal; // optional

}
