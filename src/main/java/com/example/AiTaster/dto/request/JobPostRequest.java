package com.example.AiTaster.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobPostRequest {
    @NotBlank(message = "FIELD_REQUIRED")
    String title;

    @NotBlank(message = "FIELD_REQUIRED")
    String requirementDescription;

    @NotBlank(message = "FIELD_REQUIRED")
    String businessGoal;

    @NotBlank(message = "FIELD_REQUIRED")
    String mainFeatures;


    // Budget không được null
    @DecimalMin(value = "0.0", inclusive = false, message = "BUDGETS_INVALID") // Budget phải lớn hơn 0
    BigDecimal budgets;

    @NotBlank(message = "FIELD_REQUIRED")
    String timeLine;


    List<Long> selectedSkillIds;

}
