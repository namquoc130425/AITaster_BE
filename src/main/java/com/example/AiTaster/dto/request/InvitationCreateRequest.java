package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class InvitationCreateRequest {
    @NotNull(message = "FIELD_REQUIRED")
    Long applicationId;

    @NotBlank(message = "FIELD_REQUIRED")
    String projectTitle;

    @NotBlank(message = "FIELD_REQUIRED")
    String finalRequirement;

    @NotBlank(message = "FIELD_REQUIRED")
    String expectedOutput;

    @NotBlank(message = "FIELD_REQUIRED")
    String acceptanceCriteria;

    @NotNull(message = "FIELD_REQUIRED")
    @DecimalMin(value = "0.0", inclusive = false, message = "PRICE_INVALID")
    BigDecimal finalOfferedPrice;

    @NotBlank(message = "FIELD_REQUIRED")
    String finalTimeline;


    @NotNull(message = "FIELD_REQUIRED")
    @AssertTrue(message = "CLIENT_TERMS_REQUIRED")  // phải trả về true , trả về false là báo lỗi
    Boolean clientAcceptedTerms;

}
