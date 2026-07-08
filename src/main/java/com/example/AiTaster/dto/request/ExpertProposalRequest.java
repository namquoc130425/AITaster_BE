package com.example.AiTaster.dto.request;

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
public class ExpertProposalRequest {

    @NotBlank(message = "FIELD_REQUIRED")
    String title;

    String technologies;

    @NotBlank(message = "FIELD_REQUIRED")
    String detailContent;

    @NotNull(message = "FIELD_REQUIRED")
    @DecimalMin(value = "0.0", inclusive = true, message = "PRICE_INVALID")
    BigDecimal priceToUnlock;
}
