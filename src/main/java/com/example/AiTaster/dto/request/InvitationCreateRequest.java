package com.example.AiTaster.dto.request;

import com.example.AiTaster.constant.TimelineUnit;
import jakarta.validation.constraints.*;
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


    // Client nhập số thời gian hoàn thành.
    @NotNull(message = "FIELD_REQUIRED")
    @Min(value = 1, message = "TIMELINE_VALUE_INVALID")
    Integer finalTimelineValue;

    // Client chọn đơn vị trên dropdown.
    @NotNull(message = "FIELD_REQUIRED")
    TimelineUnit finalTimelineUnit;


    @NotNull(message = "FIELD_REQUIRED")
    @AssertTrue(message = "CLIENT_TERMS_REQUIRED")  // phải trả về true , trả về false là báo lỗi
    Boolean clientAcceptedTerms;

}
