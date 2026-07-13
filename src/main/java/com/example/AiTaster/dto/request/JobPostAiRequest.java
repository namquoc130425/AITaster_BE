package com.example.AiTaster.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobPostAiRequest {
    @NotBlank(message = "TITLE_REQUIRED")
    String title;

    String requirementDescription;

    String businessGoal;

    String mainFeatures;

    @DecimalMin(value = "0.0", inclusive = false, message = "BUDGETS_INVALID") // Budget phải lớn hơn 0
    BigDecimal budgets;

    String timeLine;

    List<Long> selectedSkillIds;
}
